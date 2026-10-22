package dev.grzybalson;

import com.zaxxer.hikari.HikariConfig;
import dev.grzybalson.model.*;
import dev.grzybalson.service.*;
import dev.grzybalson.storage.SqlWalletRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class WalletStorageTest {
    @TempDir Path directory;
    private SqlWalletRepository repository;
    private WalletService service;
    private final PlayerIdentity player = new PlayerIdentity(UUID.randomUUID(), "TestGracz");

    @BeforeEach void open() {
        HikariConfig pool = new HikariConfig();
        pool.setJdbcUrl("jdbc:sqlite:" + directory.resolve("wallet.db"));
        pool.setMaximumPoolSize(1);
        pool.setConnectionInitSql("PRAGMA busy_timeout=10000");
        repository = new SqlWalletRepository(pool, "test_", true);
        service = new WalletService(repository, Clock.fixed(Instant.ofEpochMilli(1_000_000L), ZoneOffset.UTC), (min, max) -> min);
    }
    @AfterEach void close() { repository.close(); }

    @Test void persistsOfflineBalanceAndCooldownAcrossReopen() {
        service.change(player, 1500, "ADD", "CONSOLE");
        service.reward(player, 10, 50, 86_400_000L);
        repository.close(); open();
        assertEquals(1510, service.load(player).balance);
        assertEquals(player.id, repository.findByName("testgracz").id);
        assertEquals(87_400_000L, service.load(player).nextRewardAt);
        assertEquals("rewardCooldown", assertThrows(WalletException.class, () -> service.reward(player, 10, 50, 86_400_000L)).key);
        PlayerIdentity renamed = new PlayerIdentity(player.id, "NowyNick");
        assertEquals(1510, service.load(renamed).balance);
        assertEquals(player.id, repository.findByName("NowyNick").id);
        assertNull(repository.findByName("TestGracz"));
        assertEquals(-100, service.change(renamed, -100, "SET", "CONSOLE").account.balance);
        assertEquals(-100, service.change(renamed, 0, "ADD", "CONSOLE").account.balance);
        assertEquals(-99, service.change(renamed, 1, "ADD", "CONSOLE").account.balance);
        assertEquals(1, service.change(renamed, -100, "REMOVE", "CONSOLE").account.balance);
    }

    @Test void concurrentRewardCanBeClaimedOnlyOnce() throws Exception {
        List<Callable<Boolean>> jobs = new ArrayList<>();
        for (int i = 0; i < 16; i++) jobs.add(() -> {
            try { service.reward(player, 10, 50, 86_400_000L); return true; }
            catch (WalletException exception) { assertEquals("rewardCooldown", exception.key); return false; }
        });
        assertEquals(1, run(jobs));
        assertEquals(10, service.load(player).balance);
        assertEquals(1, repository.logs(player.id, 0, 100).size());
    }

    @Test void competingPurchasesNeverOverspend() throws Exception {
        service.change(player, 500, "SET", "CONSOLE");
        List<Callable<Boolean>> jobs = new ArrayList<>();
        for (int i = 0; i < 12; i++) jobs.add(() -> {
            try { service.purchase(player, 100, "rangi/vip"); return true; }
            catch (WalletException exception) { assertEquals("insufficient", exception.key); return false; }
        });
        assertEquals(5, run(jobs));
        assertEquals(0, service.load(player).balance);
        assertEquals(6, repository.logs(player.id, 0, 100).size());
        repository.recover();
        assertEquals(5, repository.logs(player.id, 0, 100).stream().filter(log -> log.status.equals("REVIEW")).count());
    }

    @Test void failedAuditInsertRollsBackBalance() {
        WalletService.Result result = service.change(player, 100, "ADD", "Admin");
        Account before = service.load(player);
        Account after = before.next(player.name, 999, 0);
        assertThrows(IllegalStateException.class, () -> repository.commit(before, after, result.log));
        assertEquals(100, service.load(player).balance);
        assertEquals(before.version, service.load(player).version);
    }

    private int run(List<Callable<Boolean>> jobs) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            int count = 0;
            for (Future<Boolean> job : executor.invokeAll(jobs)) if (job.get()) count++;
            return count;
        } finally { executor.shutdownNow(); }
    }
}
