package dev.grzybalson.service;

import dev.grzybalson.config.WalletConfig;
import dev.grzybalson.gui.*;
import dev.grzybalson.model.*;
import dev.grzybalson.permission.WalletPermissions;
import dev.grzybalson.logging.WalletLogger;
import dev.grzybalson.util.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import java.util.*;
import java.util.function.Supplier;

public final class WalletActions {
    private final Supplier<WalletConfig> config;
    private final WalletService service;
    private final TaskUtil tasks;
    private final MessageService messages;
    private final PlayerDirectory directory;
    private final TopupBroadcast broadcast;
    private final WalletLogger logger;
    private final Set<UUID> busy = new HashSet<>();
    private WalletMenus menus;

    public WalletActions(Supplier<WalletConfig> config, WalletService service, TaskUtil tasks, MessageService messages, PlayerDirectory directory, WalletLogger logger) {
        this.config = config; this.service = service; this.tasks = tasks; this.messages = messages; this.directory = directory; this.logger = logger;
        this.broadcast = new TopupBroadcast(config, logger);
    }
    public void bind(WalletMenus menus) { this.menus = menus; }

    public void modify(CommandSender sender, String mode, String name, String rawAmount) {
        long amount;
        try {
            amount = MoneyUtil.parse(rawAmount);
        } catch (IllegalArgumentException exception) { messages.error(sender, config.get().messages.invalidAmount); return; }
        if (!name.matches("[A-Za-z0-9_]{1,16}")) {
            messages.error(sender, config.get().messages.unknownPlayer, PlaceholderUtil.values("player", name)); return;
        }
        PlayerIdentity known = directory.find(name);
        String actor = sender.getName();
        tasks.submit(() -> service.change(service.resolve(name, known), amount, mode, actor), result -> {
            messages.success(sender, config.get().messages.changed, PlaceholderUtil.values("player", result.account.name, "hollywallet_wpln", MoneyUtil.format(result.account.balance)));
            if (mode.equals("TOPUP") && amount > 0) broadcast.announce(result.account.name, amount);
        }, exception -> messages.failure(sender, exception));
    }

    public void logs(Player viewer, String name) { menus.logs(viewer, name, directory.find(name), 0); }

    public void daily(Player player) {
        if (!player.hasPermission(WalletPermissions.USE)) { messages.error(player, config.get().messages.noPermission); return; }
        WalletConfig.Reward reward = config.get().reward;
        if (!reward.enabled) { messages.error(player, config.get().messages.rewardDisabled); return; }
        if (!lock(player)) return;
        PlayerIdentity identity = identity(player);
        InventoryHolder view = menus.current(player);
        long min = MoneyUtil.parse(reward.min), max = MoneyUtil.parse(reward.max), cooldown = reward.cooldownHours * 3_600_000L;
        tasks.submit(() -> service.reward(identity, min, max, cooldown), result -> {
            busy.remove(identity.id);
            if (!player.isOnline()) return;
            messages.success(player, config.get().messages.rewardReceived, PlaceholderUtil.values("amount", MoneyUtil.format(result.log.amount)));
            menus.refresh(player, view);
        }, exception -> { busy.remove(identity.id); if (player.isOnline()) messages.failure(player, exception); });
    }

    public void purchase(Player player, PurchaseRequest request) {
        if (!player.hasPermission(WalletPermissions.USE)) { messages.error(player, config.get().messages.noPermission); return; }
        WalletConfig.Category category = config.get().categories.get(request.category);
        WalletConfig.Product product = category == null ? null : category.products.get(request.productId);
        if (product == null || MoneyUtil.parse(product.price) != request.price || !product.commands.equals(request.commands) || !available(request)) {
            messages.error(player, config.get().messages.unavailable); return;
        }
        if (!lock(player)) return;
        PlayerIdentity identity = identity(player);
        tasks.submit(() -> service.purchase(identity, request.price, request.category + "/" + request.productId + " | " + request.displayname),
            result -> deliver(player, identity, request, result), exception -> { busy.remove(identity.id); if (player.isOnline()) messages.failure(player, exception); });
    }

    private boolean available(PurchaseRequest request) {
        if (request.commands.isEmpty()) return false;
        for (String command : request.commands) {
            String label = command.trim().replaceFirst("^/", "").split("\\s+", 2)[0];
            if (Bukkit.getPluginCommand(label) == null && Bukkit.getHelpMap().getHelpTopic("/" + label) == null) return false;
        }
        return true;
    }

    private void deliver(Player player, PlayerIdentity identity, PurchaseRequest request, WalletService.Result result) {
        boolean success = true;
        Map<String, String> values = PlaceholderUtil.values("player", identity.name, "uuid", identity.id.toString(), "holly_cost", MoneyUtil.format(request.price));
        try {
            for (String command : request.commands) {
                if (!Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderUtil.replace(command.trim().replaceFirst("^/", ""), values))) { success = false; break; }
            }
        } catch (RuntimeException exception) { success = false; logger.error("DOSTAWA", "Dostawa wymaga sprawdzenia. Gracz: " + identity.name + " · zakup: " + result.log.id, exception); }
        String status = success ? "COMPLETED" : "REVIEW";
        tasks.submit(() -> { service.status(identity.id, result.log.id, status); return status; }, state -> {
            busy.remove(identity.id);
            if (state.equals("REVIEW")) logger.warn("DOSTAWA", "Sprawdź nadanie produktu dla " + identity.name + ". Identyfikator zakupu: " + result.log.id);
            if (!player.isOnline()) return;
            if (state.equals("COMPLETED")) {
                messages.success(player, config.get().messages.purchased, PlaceholderUtil.values("product", request.displayname, "holly_cost", MoneyUtil.format(request.price)));
            } else messages.error(player, config.get().messages.purchaseReview, PlaceholderUtil.values("id", result.log.id));
        }, exception -> {
            busy.remove(identity.id);
            logger.error("AUDYT", "Nie zapisano statusu zakupu " + result.log.id + ". Sprawdź dostawę przed zwrotem lub ponownym nadaniem.", exception);
            if (player.isOnline()) messages.error(player, config.get().messages.purchaseReview, PlaceholderUtil.values("id", result.log.id));
        });
    }

    private boolean lock(Player player) {
        if (busy.add(player.getUniqueId())) return true;
        messages.error(player, config.get().messages.busy);
        return false;
    }
    private PlayerIdentity identity(Player player) { return new PlayerIdentity(player.getUniqueId(), player.getName()); }
}
