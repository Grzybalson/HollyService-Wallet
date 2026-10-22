package dev.grzybalson.service;

import dev.grzybalson.model.PlayerIdentity;
import org.bukkit.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerDirectory {
    private final Map<String, PlayerIdentity> known = new ConcurrentHashMap<>();
    private final Map<UUID, String> names = new ConcurrentHashMap<>();
    public PlayerDirectory() { for (OfflinePlayer player : Bukkit.getOfflinePlayers()) remember(player); }
    public void remember(OfflinePlayer player) {
        if (player.getName() == null) return;
        String lower = player.getName().toLowerCase(Locale.ROOT);
        String previous = names.put(player.getUniqueId(), lower);
        if (previous != null && !previous.equals(lower)) known.remove(previous);
        known.put(lower, new PlayerIdentity(player.getUniqueId(), player.getName()));
    }
    public PlayerIdentity find(String name) { return known.get(name.toLowerCase(Locale.ROOT)); }
    public List<String> suggestions() {
        List<String> result = new ArrayList<>();
        for (PlayerIdentity identity : known.values()) result.add(identity.name);
        result.sort(String.CASE_INSENSITIVE_ORDER);
        return result;
    }
}
