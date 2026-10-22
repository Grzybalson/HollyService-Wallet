package dev.grzybalson.service;

import dev.grzybalson.config.WalletConfig;
import dev.grzybalson.logging.WalletLogger;
import dev.grzybalson.util.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.Map;
import java.util.function.Supplier;

public final class TopupBroadcast {
    private final Supplier<WalletConfig> config;
    private final WalletLogger logger;
    public TopupBroadcast(Supplier<WalletConfig> config, WalletLogger logger) { this.config = config; this.logger = logger; }
    public void announce(String name, long amount) {
        WalletConfig.Broadcast settings = config.get().topup;
        Map<String, String> values = PlaceholderUtil.values("player", name, "iloscwpln", MoneyUtil.format(amount));
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                player.sendTitle(ColorUtil.color(PlaceholderUtil.replace(settings.title, values)), ColorUtil.color(PlaceholderUtil.replace(settings.subtitle, values)), settings.fadeIn, settings.stay, settings.fadeOut);
                if (!settings.sound.isEmpty()) player.playSound(player.getLocation(), settings.sound, settings.volume, settings.pitch);
            } catch (RuntimeException exception) { logger.warn("POWIADOMIENIE", "Gracz " + player.getName() + " nie otrzymał efektu doładowania: " + exception.getMessage()); }
        }
    }
}
