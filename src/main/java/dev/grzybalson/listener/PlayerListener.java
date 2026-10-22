package dev.grzybalson.listener;

import dev.grzybalson.gui.WalletMenus;
import dev.grzybalson.model.PlayerIdentity;
import dev.grzybalson.service.*;
import dev.grzybalson.util.TaskUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

public final class PlayerListener implements Listener {
    private final PlayerDirectory directory;
    private final WalletService service;
    private final TaskUtil tasks;
    private final MessageService messages;
    private final WalletMenus menus;
    public PlayerListener(PlayerDirectory directory, WalletService service, TaskUtil tasks, MessageService messages, WalletMenus menus) {
        this.directory = directory; this.service = service; this.tasks = tasks; this.messages = messages; this.menus = menus;
    }
    @EventHandler public void join(PlayerJoinEvent event) { load(event.getPlayer()); }
    public void load(Player player) {
        directory.remember(player);
        PlayerIdentity identity = new PlayerIdentity(player.getUniqueId(), player.getName());
        tasks.submit(() -> service.load(identity), result -> {}, exception -> { if (player.isOnline()) messages.failure(player, exception); });
    }
    @EventHandler public void quit(PlayerQuitEvent event) {
        service.evict(event.getPlayer().getUniqueId()); menus.forget(event.getPlayer());
    }
}
