package dev.grzybalson.placeholder;

import dev.grzybalson.model.*;
import dev.grzybalson.service.WalletService;
import dev.grzybalson.util.*;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class WalletExpansion extends PlaceholderExpansion implements AutoCloseable {
    private final WalletService service;
    private final TaskUtil tasks;
    private final Set<UUID> loading = ConcurrentHashMap.newKeySet();
    private final JavaPlugin plugin;
    public WalletExpansion(WalletService service, TaskUtil tasks, JavaPlugin plugin) {
        this.service = service; this.tasks = tasks; this.plugin = plugin;
    }
    @Override public String getIdentifier() { return "hollywallet"; }
    @Override public String getAuthor() { return "grzybalson"; }
    @Override public String getVersion() { return plugin.getDescription().getVersion(); }
    @Override public String getRequiredPlugin() { return plugin.getName(); }
    @Override public boolean persist() { return true; }
    @Override public String onRequest(OfflinePlayer player, String params) {
        if (!params.equalsIgnoreCase("wpln")) return null;
        if (player == null) return "0.00";
        Account account = service.cached(player.getUniqueId());
        if (account != null) return MoneyUtil.format(account.balance);
        if (player.getName() != null && loading.add(player.getUniqueId())) {
            PlayerIdentity identity = new PlayerIdentity(player.getUniqueId(), player.getName());
            tasks.submit(() -> service.load(identity), loaded -> loading.remove(identity.id), error -> loading.remove(identity.id));
        }
        return "0.00";
    }
    @Override public String onPlaceholderRequest(Player player, String params) { return onRequest(player, params); }
    @Override public void close() {
        loading.clear();
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") &&
            PlaceholderAPIPlugin.getInstance().getLocalExpansionManager().getExpansion(getIdentifier()) == this) unregister();
    }
}
