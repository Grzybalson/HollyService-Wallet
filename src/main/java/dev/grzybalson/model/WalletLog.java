package dev.grzybalson.model;

import java.util.UUID;

public final class WalletLog {
    public final String id;
    public final UUID playerId;
    public final long timestamp;
    public final String type;
    public final String product;
    public final long amount;
    public final long balance;
    public final String actor;
    public final String status;
    public WalletLog(String id, UUID playerId, long timestamp, String type, String product, long amount, long balance, String actor, String status) {
        this.id = id; this.playerId = playerId; this.timestamp = timestamp; this.type = type; this.product = product;
        this.amount = amount; this.balance = balance; this.actor = actor; this.status = status;
    }
}
