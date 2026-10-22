package dev.grzybalson.storage;

import com.zaxxer.hikari.HikariConfig;
import dev.grzybalson.config.DatabaseConfig;
import dev.grzybalson.logging.WalletLogger;
import java.io.File;
import java.nio.file.Files;
import java.util.Locale;

public final class RepositoryFactory {
    private RepositoryFactory() {}
    public static WalletRepository create(DatabaseConfig config, File folder, WalletLogger logger) throws Exception {
        String type = config.type.toUpperCase(Locale.ROOT);
        if (type.equals("MONGODB")) return new MongoWalletRepository(config.mongo.uri, config.mongo.database, config.mongo.collectionPrefix, logger);
        HikariConfig pool = new HikariConfig();
        pool.setPoolName("HollyWallet");
        pool.setConnectionTimeout(10_000);
        if (type.equals("SQLITE")) {
            if (!config.sqliteFile.matches("[A-Za-z0-9._-]+") || config.sqliteFile.equals(".") || config.sqliteFile.equals("..")) throw new IllegalArgumentException("Niepoprawna nazwa pliku SQLite");
            File directory = new File(folder, "database");
            Files.createDirectories(directory.toPath());
            pool.setJdbcUrl("jdbc:sqlite:" + new File(directory, config.sqliteFile).getAbsolutePath());
            pool.setDriverClassName("org.sqlite.JDBC");
            pool.setMaximumPoolSize(1);
            pool.setConnectionInitSql("PRAGMA busy_timeout=10000");
            return new SqlWalletRepository(pool, "wallet_", true);
        }
        if (type.equals("MYSQL")) {
            DatabaseConfig.Mysql mysql = config.mysql;
            if (!mysql.database.matches("[A-Za-z0-9_]+")) throw new IllegalArgumentException("Niepoprawna nazwa bazy MySQL");
            pool.setJdbcUrl("jdbc:mysql://" + mysql.host + ":" + mysql.port + "/" + mysql.database + "?useSSL=" + mysql.ssl + "&characterEncoding=UTF-8&serverTimezone=UTC&connectTimeout=5000&socketTimeout=10000");
            pool.setDriverClassName("com.mysql.cj.jdbc.Driver");
            pool.setUsername(mysql.username);
            pool.setPassword(mysql.password);
            pool.setMaximumPoolSize(Math.max(1, mysql.poolSize));
            return new SqlWalletRepository(pool, mysql.tablePrefix, false);
        }
        throw new IllegalArgumentException("Nieznany typ bazy: " + config.type);
    }
}
