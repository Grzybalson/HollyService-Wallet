package dev.grzybalson.config;

import eu.okaeri.configs.OkaeriConfig;

public class DatabaseConfig extends OkaeriConfig {
    public String type = "SQLITE";
    public String sqliteFile = "wallet.db";
    public Mysql mysql = new Mysql();
    public Mongo mongo = new Mongo();

    public static class Mysql extends OkaeriConfig {
        public String host = "localhost";
        public int port = 3306;
        public String database = "holly_wallet";
        public String username = "root";
        public String password = "";
        public boolean ssl = false;
        public String tablePrefix = "holly_wallet_";
        public int poolSize = 4;
    }

    public static class Mongo extends OkaeriConfig {
        public String uri = "mongodb://localhost:27017/?serverSelectionTimeoutMS=5000";
        public String database = "holly_wallet";
        public String collectionPrefix = "wallet_";
    }
}
