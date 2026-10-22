package dev.grzybalson.storage;

import dev.grzybalson.model.*;
import java.util.*;

public interface WalletRepository extends AutoCloseable {
    Account load(UUID id, String name);
    Account findByName(String name);
    boolean commit(Account before, Account after, WalletLog log);
    List<WalletLog> logs(UUID player, int offset, int limit);
    void status(UUID player, String logId, String status);
    void recover();
    void close();
}
