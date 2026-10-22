package dev.grzybalson;

import dev.grzybalson.command.*;
import dev.grzybalson.config.*;
import dev.grzybalson.gui.WalletMenus;
import dev.grzybalson.listener.PlayerListener;
import dev.grzybalson.placeholder.WalletExpansion;
import dev.grzybalson.permission.WalletPermissions;
import dev.grzybalson.logging.WalletLogger;
import dev.grzybalson.service.*;
import dev.grzybalson.storage.*;
import dev.grzybalson.util.*;
import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.*;
import dev.rollczi.litecommands.suggestion.SuggestionResult;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.*;
import org.bukkit.event.server.PluginEnableEvent;
import java.io.File;
import java.nio.file.Files;

public class WalletPlugin extends JavaPlugin {
    private volatile WalletConfig settings;
    private WalletRepository repository;
    private TaskUtil tasks;
    private WalletMenus menus;
    private MessageService messages;
    private LiteCommands<CommandSender> commands;
    private WalletExpansion expansion;
    private WalletPermissions permissions;
    private WalletLogger logger;

    @Override public void onEnable() {
        long startedAt = System.nanoTime();
        logger = new WalletLogger(this);
        try {
            Files.createDirectories(getDataFolder().toPath());
            settings = ConfigLoader.load(WalletConfig.class, new File(getDataFolder(), "wallet.yml"));
            ConfigLoader.validate(settings);
            DatabaseConfig database = ConfigLoader.load(DatabaseConfig.class, new File(getDataFolder(), "database.yml"));
            repository = RepositoryFactory.create(database, getDataFolder(), logger);
            repository.recover();
            tasks = new TaskUtil(this);
            WalletService service = new WalletService(repository);
            PlayerDirectory directory = new PlayerDirectory();
            messages = new MessageService(() -> settings, logger);
            WalletActions actions = new WalletActions(() -> settings, service, tasks, messages, directory, logger);
            menus = new WalletMenus(() -> settings, service, tasks, messages, actions::daily, actions::purchase);
            actions.bind(menus);
            permissions = new WalletPermissions(getServer().getPluginManager());
            PlayerListener players = new PlayerListener(directory, service, tasks, messages, menus);
            getServer().getPluginManager().registerEvents(menus, this);
            getServer().getPluginManager().registerEvents(players, this);
            commands = LiteBukkitFactory.builder("hollywallet", this)
                .settings(options -> options.nativePermissions(false))
                .commands(new WalletCommand(menus), new AdminWalletCommand(actions, this::reloadWallet))
                .argumentSuggester(String.class, (invocation, argument, context) -> {
                    if (argument.getName().equals("nick")) return SuggestionResult.of(directory.suggestions());
                    if (argument.getName().equals("ilosc")) return SuggestionResult.of("0", "-1", "0.01", "1", "10", "100");
                    return SuggestionResult.empty();
                })
                .message(LiteBukkitMessages.PLAYER_ONLY, ignored -> ColorUtil.color(settings.messages.prefix + settings.messages.playerOnly))
                .missingPermission((invocation, result, chain) -> messages.error(invocation.sender(), settings.messages.noPermission))
                .invalidUsage((invocation, result, chain) -> {
                    for (String usage : result.getSchematic().all()) messages.error(invocation.sender(), settings.messages.usage, PlaceholderUtil.values("usage", usage));
                }).build();
            hookPlaceholders(service);
            getServer().getPluginManager().registerEvents(new Listener() {
                @EventHandler public void enabled(PluginEnableEvent event) {
                    if (event.getPlugin().getName().equalsIgnoreCase("PlaceholderAPI")) tasks.later(() -> hookPlaceholders(service));
                }
            }, this);
            for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) players.load(player);
            Bukkit.getScheduler().runTaskTimer(this, () -> {
                for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) players.load(player);
            }, 600L, 600L);
            logger.ready(database.type, settings.rows, settings.categories.size(), (System.nanoTime() - startedAt) / 1_000_000);
        } catch (Exception exception) {
            logger.error("START", "Uruchomienie portfela przerwane. Sprawdź konfigurację i połączenie z bazą.", exception);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void hookPlaceholders(WalletService service) {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) return;
        if (expansion != null) expansion.close();
        WalletExpansion candidate = new WalletExpansion(service, tasks, this);
        if (candidate.register()) expansion = candidate;
        else logger.warn("PLACEHOLDER", "Nie udało się zarejestrować %hollywallet_wpln%.");
    }

    private void reloadWallet(CommandSender sender) {
        try {
            WalletConfig next = ConfigLoader.load(WalletConfig.class, new File(getDataFolder(), "wallet.yml"));
            ConfigLoader.validate(next);
            menus.closeAll();
            settings = next;
            messages.success(sender, settings.messages.reloaded);
        } catch (Exception exception) { messages.failure(sender, exception); }
    }

    @Override public void onDisable() {
        if (commands != null) commands.unregister();
        if (permissions != null) permissions.close();
        if (expansion != null) expansion.close();
        if (menus != null) menus.closeAll();
        if (tasks != null) tasks.close();
        if (repository != null) repository.close();
        if (logger != null) logger.info("STOP", "Połączenia zamknięte. Portfel został wyłączony.");
    }
}
