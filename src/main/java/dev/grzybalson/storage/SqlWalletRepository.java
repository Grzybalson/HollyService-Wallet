package dev.grzybalson.storage;

import com.zaxxer.hikari.*;
import dev.grzybalson.model.*;
import java.sql.*;
import java.util.*;

public final class SqlWalletRepository implements WalletRepository {
    private final HikariDataSource source;
    private final String accounts;
    private final String history;
    private final boolean sqlite;

    public SqlWalletRepository(HikariConfig config, String prefix, boolean sqlite) {
        if (!prefix.matches("[A-Za-z0-9_]+")) throw new IllegalArgumentException("Niepoprawny prefiks tabel");
        this.sqlite = sqlite;
        this.accounts = prefix + "accounts";
        this.history = prefix + "history";
        this.source = new HikariDataSource(config);
        try (Connection connection = source.getConnection(); Statement statement = connection.createStatement()) {
            if (sqlite) statement.execute("PRAGMA journal_mode=WAL");
            String storage = sqlite ? "" : " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            statement.execute("CREATE TABLE IF NOT EXISTS " + accounts + " (uuid VARCHAR(36) PRIMARY KEY, name VARCHAR(16) NOT NULL, name_lower VARCHAR(16) NOT NULL, balance BIGINT NOT NULL, next_reward BIGINT NOT NULL, version BIGINT NOT NULL)" + storage);
            statement.execute("CREATE TABLE IF NOT EXISTS " + history + " (id VARCHAR(36) PRIMARY KEY, player_uuid VARCHAR(36) NOT NULL, created_at BIGINT NOT NULL, type VARCHAR(24) NOT NULL, product TEXT NOT NULL, amount BIGINT NOT NULL, balance BIGINT NOT NULL, actor VARCHAR(64) NOT NULL, status VARCHAR(24) NOT NULL)" + storage);
            if (sqlite) {
                statement.execute("CREATE INDEX IF NOT EXISTS " + prefix + "name_idx ON " + accounts + " (name_lower)");
                statement.execute("CREATE INDEX IF NOT EXISTS " + prefix + "history_idx ON " + history + " (player_uuid, created_at)");
            } else {
                ensureIndex(connection, accounts, prefix + "name_idx", "name_lower");
                ensureIndex(connection, history, prefix + "history_idx", "player_uuid, created_at");
            }
        } catch (SQLException exception) { source.close(); throw failure(exception); }
    }

    private void ensureIndex(Connection connection, String table, String name, String columns) throws SQLException {
        try (ResultSet result = connection.getMetaData().getIndexInfo(connection.getCatalog(), null, table, false, false)) {
            while (result.next()) if (name.equalsIgnoreCase(result.getString("INDEX_NAME"))) return;
        }
        try (Statement statement = connection.createStatement()) { statement.execute("CREATE INDEX " + name + " ON " + table + " (" + columns + ")"); }
    }

    @Override public Account load(UUID id, String name) {
        try (Connection connection = source.getConnection()) {
            String insert = (sqlite ? "INSERT OR IGNORE INTO " : "INSERT IGNORE INTO ") + accounts + " (uuid,name,name_lower,balance,next_reward,version) VALUES (?,?,?,0,0,0)";
            try (PreparedStatement statement = connection.prepareStatement(insert)) {
                statement.setString(1, id.toString()); statement.setString(2, name); statement.setString(3, name.toLowerCase(Locale.ROOT)); statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement("UPDATE " + accounts + " SET name=?,name_lower=? WHERE uuid=?")) {
                statement.setString(1, name); statement.setString(2, name.toLowerCase(Locale.ROOT)); statement.setString(3, id.toString()); statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + accounts + " WHERE uuid=?")) {
                statement.setString(1, id.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) throw new SQLException("Brak konta po utworzeniu");
                    return account(result);
                }
            }
        } catch (SQLException exception) { throw failure(exception); }
    }

    @Override public Account findByName(String name) {
        try (Connection connection = source.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + accounts + " WHERE name_lower=?")) {
            statement.setString(1, name.toLowerCase(Locale.ROOT));
            try (ResultSet result = statement.executeQuery()) { return result.next() ? account(result) : null; }
        } catch (SQLException exception) { throw failure(exception); }
    }

    @Override public boolean commit(Account before, Account after, WalletLog log) {
        try (Connection connection = source.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement("UPDATE " + accounts + " SET balance=?,next_reward=?,version=? WHERE uuid=? AND version=?")) {
                    statement.setLong(1, after.balance); statement.setLong(2, after.nextRewardAt); statement.setLong(3, after.version);
                    statement.setString(4, before.id.toString()); statement.setLong(5, before.version);
                    if (statement.executeUpdate() != 1) { connection.rollback(); return false; }
                }
                try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + history + " (id,player_uuid,created_at,type,product,amount,balance,actor,status) VALUES (?,?,?,?,?,?,?,?,?)")) {
                    statement.setString(1, log.id); statement.setString(2, log.playerId.toString()); statement.setLong(3, log.timestamp);
                    statement.setString(4, log.type); statement.setString(5, log.product); statement.setLong(6, log.amount);
                    statement.setLong(7, log.balance); statement.setString(8, log.actor); statement.setString(9, log.status); statement.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (SQLException exception) { connection.rollback(); throw exception; }
        } catch (SQLException exception) { throw failure(exception); }
    }

    @Override public List<WalletLog> logs(UUID player, int offset, int limit) {
        List<WalletLog> logs = new ArrayList<>();
        try (Connection connection = source.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + history + " WHERE player_uuid=? ORDER BY created_at DESC,id DESC LIMIT ? OFFSET ?")) {
            statement.setString(1, player.toString()); statement.setInt(2, limit); statement.setInt(3, offset);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) logs.add(new WalletLog(result.getString("id"), player, result.getLong("created_at"), result.getString("type"), result.getString("product"), result.getLong("amount"), result.getLong("balance"), result.getString("actor"), result.getString("status")));
            }
            return logs;
        } catch (SQLException exception) { throw failure(exception); }
    }

    @Override public void status(UUID player, String id, String status) {
        try (Connection connection = source.getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE " + history + " SET status=? WHERE id=? AND player_uuid=?")) {
            statement.setString(1, status); statement.setString(2, id); statement.setString(3, player.toString()); statement.executeUpdate();
        } catch (SQLException exception) { throw failure(exception); }
    }

    @Override public void recover() {
        try (Connection connection = source.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE " + history + " SET status='REVIEW' WHERE status='PENDING'");
        } catch (SQLException exception) { throw failure(exception); }
    }

    private Account account(ResultSet result) throws SQLException {
        return new Account(UUID.fromString(result.getString("uuid")), result.getString("name"), result.getLong("balance"), result.getLong("next_reward"), result.getLong("version"));
    }
    private IllegalStateException failure(SQLException exception) { return new IllegalStateException("Blad bazy portfela", exception); }
    @Override public void close() { source.close(); }
}
