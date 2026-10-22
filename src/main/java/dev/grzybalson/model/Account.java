package dev.grzybalson.model;

import java.util.UUID;

public final class Account {
    public final UUID id;
    public final String name;
    public final long balance;
    public final long nextRewardAt;
    public final long version;
    public Account(UUID id, String name, long balance, long nextRewardAt, long version) {
        this.id = id; this.name = name; this.balance = balance; this.nextRewardAt = nextRewardAt; this.version = version;
    }
    public Account next(String name, long balance, long nextRewardAt) {
        return new Account(id, name, balance, nextRewardAt, version + 1);
    }
}
