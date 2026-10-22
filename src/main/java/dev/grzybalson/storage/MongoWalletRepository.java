package dev.grzybalson.storage;

import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.WriteConcern;
import dev.grzybalson.model.*;
import dev.grzybalson.logging.WalletLogger;
import org.bson.Document;
import java.util.*;
import static com.mongodb.client.model.Filters.*;

public final class MongoWalletRepository implements WalletRepository {
    private final MongoClient client;
    private final MongoCollection<Document> accounts;
    private final MongoCollection<Document> history;
    private final WalletLogger logger;

    public MongoWalletRepository(String uri, String database, String prefix, WalletLogger logger) {
        this.logger = logger;
        client = MongoClients.create(MongoClientSettings.builder().applyConnectionString(new ConnectionString(uri))
            .writeConcern(WriteConcern.MAJORITY.withJournal(true)).build());
        MongoDatabase db = client.getDatabase(database);
        try {
            db.runCommand(new Document("ping", 1));
            accounts = db.getCollection(prefix + "accounts");
            history = db.getCollection(prefix + "history");
            accounts.createIndex(Indexes.ascending("nameLower"));
            history.createIndex(Indexes.compoundIndex(Indexes.ascending("player"), Indexes.descending("timestamp", "_id")));
        } catch (RuntimeException exception) { client.close(); throw exception; }
    }

    @Override public Account load(UUID id, String name) {
        Document defaults = new Document("balance", 0L).append("nextReward", 0L).append("version", 0L).append("outbox", new ArrayList<Document>());
        Document updates = new Document("$setOnInsert", defaults).append("$set", new Document("name", name).append("nameLower", name.toLowerCase(Locale.ROOT)));
        Document doc = accounts.findOneAndUpdate(eq("_id", id.toString()), updates, new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER));
        return account(Objects.requireNonNull(doc));
    }

    @Override public Account findByName(String name) {
        Document doc = accounts.find(eq("nameLower", name.toLowerCase(Locale.ROOT))).first();
        return doc == null ? null : account(doc);
    }

    @Override public boolean commit(Account before, Account after, WalletLog log) {
        Document updates = new Document("$set", new Document("balance", after.balance).append("nextReward", after.nextRewardAt).append("version", after.version))
            .append("$push", new Document("outbox", document(log)));
        boolean changed = accounts.updateOne(and(eq("_id", before.id.toString()), eq("version", before.version)), updates).getModifiedCount() == 1;
        if (changed) {
            try { drain(before.id); }
            catch (RuntimeException exception) { logger.warn("AUDYT", "Historia gracza " + before.id + " oczekuje na ponowny zapis. Saldo jest zapisane."); }
        }
        return changed;
    }

    private void drain(UUID id) {
        Document doc = accounts.find(eq("_id", id.toString())).first();
        if (doc == null) return;
        List<Document> pending = doc.getList("outbox", Document.class, Collections.emptyList());
        for (Document event : pending) {
            history.updateOne(eq("_id", event.getString("_id")), new Document("$setOnInsert", event), new UpdateOptions().upsert(true));
            accounts.updateOne(eq("_id", id.toString()), new Document("$pull", new Document("outbox", new Document("_id", event.getString("_id")))));
        }
    }

    @Override public List<WalletLog> logs(UUID player, int offset, int limit) {
        drain(player);
        List<WalletLog> result = new ArrayList<>();
        for (Document doc : history.find(eq("player", player.toString())).sort(Sorts.descending("timestamp", "_id")).skip(offset).limit(limit)) {
            result.add(new WalletLog(doc.getString("_id"), player, number(doc, "timestamp"), doc.getString("type"), doc.getString("product"), number(doc, "amount"), number(doc, "balance"), doc.getString("actor"), doc.getString("status")));
        }
        return result;
    }

    @Override public void status(UUID player, String id, String status) {
        drain(player);
        history.updateOne(and(eq("_id", id), eq("player", player.toString())), Updates.set("status", status));
    }

    @Override public void recover() {
        for (Document doc : accounts.find(exists("outbox.0"))) drain(UUID.fromString(doc.getString("_id")));
        history.updateMany(eq("status", "PENDING"), Updates.set("status", "REVIEW"));
    }

    private Document document(WalletLog log) {
        return new Document("_id", log.id).append("player", log.playerId.toString()).append("timestamp", log.timestamp).append("type", log.type)
            .append("product", log.product).append("amount", log.amount).append("balance", log.balance).append("actor", log.actor).append("status", log.status);
    }
    private Account account(Document doc) {
        return new Account(UUID.fromString(doc.getString("_id")), doc.getString("name"), number(doc, "balance"), number(doc, "nextReward"), number(doc, "version"));
    }
    private long number(Document doc, String key) { return ((Number) doc.get(key)).longValue(); }
    @Override public void close() { client.close(); }
}
