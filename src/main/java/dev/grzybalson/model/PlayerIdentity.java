package dev.grzybalson.model;

import java.util.UUID;

public final class PlayerIdentity {
    public final UUID id;
    public final String name;
    public PlayerIdentity(UUID id, String name) { this.id = id; this.name = name; }
}
