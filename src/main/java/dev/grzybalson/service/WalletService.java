package dev.grzybalson.service;

import dev.grzybalson.model.*;
import dev.grzybalson.storage.WalletRepository;
import dev.grzybalson.util.*;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

public final class WalletService {
    private final WalletRepository repository;
    private final Clock clock;
    private final LongBinaryOperator random;
    private final ConcurrentMap<UUID, Account> cache = new ConcurrentHashMap<>();

    public WalletService(WalletRepository repository) {
        this(repository, Clock.systemUTC(), (min, max) -> ThreadLocalRandom.current().nextLong(min, max + 1));
    }
    public WalletService(WalletRepository repository, Clock clock, LongBinaryOperator random) {
        this.repository = repository; this.clock = clock; this.random = random;
    }
    public Account load(PlayerIdentity player) { return remember(repository.load(player.id, player.name)); }
    public Account cached(UUID player) { return cache.get(player); }
    public void evict(UUID player) { cache.remove(player); }
    public PlayerIdentity resolve(String name, PlayerIdentity known) {
        if (known != null) return known;
        Account account = repository.findByName(name);
        if (account == null) throw new WalletException("unknownPlayer", PlaceholderUtil.values("player", name));
        return new PlayerIdentity(account.id, account.name);
    }

    public Result change(PlayerIdentity player, long amount, String mode, String actor) {
        if (amount < -MoneyUtil.MAX_CENTS || amount > MoneyUtil.MAX_CENTS) throw new WalletException("invalidAmount");
        return mutate(player, before -> {
            long next;
            try {
                switch (mode) {
                    case "SET": next = amount; break;
                    case "REMOVE": next = MoneyUtil.add(before.balance, -amount); break;
                    case "ADD": case "TOPUP": next = MoneyUtil.add(before.balance, amount); break;
                    default: throw new IllegalArgumentException("Nieznana operacja " + mode);
                }
            } catch (IllegalArgumentException | ArithmeticException exception) { throw new WalletException("invalidAmount"); }
            return new Mutation(before.next(player.name, next, before.nextRewardAt), mode, "—", actor, "COMPLETED");
        });
    }

    public Result reward(PlayerIdentity player, long min, long max, long cooldownMillis) {
        long now = clock.millis();
        long amount = random.applyAsLong(min, max);
        return mutate(player, before -> {
            if (now < before.nextRewardAt) throw new WalletException("rewardCooldown", PlaceholderUtil.values("time", TimeUtil.remaining(before.nextRewardAt - now)));
            long balance;
            try { balance = MoneyUtil.add(before.balance, amount); }
            catch (IllegalArgumentException exception) { throw new WalletException("invalidAmount"); }
            return new Mutation(before.next(player.name, balance, Math.addExact(now, cooldownMillis)), "REWARD", "Codzienna nagroda", player.name, "COMPLETED");
        });
    }

    public Result purchase(PlayerIdentity player, long price, String product) {
        if (price <= 0) throw new WalletException("unavailable");
        return mutate(player, before -> {
            if (before.balance < price) throw new WalletException("insufficient", PlaceholderUtil.values("holly_cost", MoneyUtil.format(price)));
            return new Mutation(before.next(player.name, before.balance - price, before.nextRewardAt), "PURCHASE", product, player.name, "PENDING");
        });
    }

    public List<WalletLog> logs(UUID player, int page, int pageSize) { return repository.logs(player, Math.multiplyExact(page, pageSize), pageSize + 1); }
    public void status(UUID player, String log, String status) { repository.status(player, log, status); }

    private Result mutate(PlayerIdentity player, Function<Account, Mutation> action) {
        for (int attempt = 0; attempt < 32; attempt++) {
            Account before = repository.load(player.id, player.name);
            Mutation mutation = action.apply(before);
            WalletLog log = new WalletLog(UUID.randomUUID().toString(), player.id, clock.millis(), mutation.type, mutation.product,
                mutation.account.balance - before.balance, mutation.account.balance, mutation.actor, mutation.status);
            if (repository.commit(before, mutation.account, log)) return new Result(remember(mutation.account), log);
        }
        throw new WalletException("busy");
    }

    private Account remember(Account account) {
        cache.compute(account.id, (id, current) -> current == null || account.version >= current.version ? account : current);
        return account;
    }
    private static final class Mutation {
        final Account account;
        final String type, product, actor, status;
        Mutation(Account account, String type, String product, String actor, String status) {
            this.account = account; this.type = type; this.product = product; this.actor = actor; this.status = status;
        }
    }
    public static final class Result {
        public final Account account;
        public final WalletLog log;
        Result(Account account, WalletLog log) { this.account = account; this.log = log; }
    }
}
