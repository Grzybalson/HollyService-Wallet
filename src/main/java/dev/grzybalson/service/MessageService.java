package dev.grzybalson.service;

import dev.grzybalson.config.WalletConfig;
import dev.grzybalson.logging.WalletLogger;
import dev.grzybalson.util.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.function.Supplier;

public final class MessageService {
    private final Supplier<WalletConfig> config;
    private final WalletLogger logger;
    public MessageService(Supplier<WalletConfig> config, WalletLogger logger) { this.config = config; this.logger = logger; }
    public void send(CommandSender sender, String template, Map<String, String> values) {
        WalletConfig.Messages settings = config.get().messages;
        String prefix = template.equals(settings.usage) ? "" : settings.prefix;
        sender.sendMessage(ColorUtil.color(PlaceholderUtil.replace(prefix + template, values)));
    }
    public void send(CommandSender sender, String template) { send(sender, template, Collections.emptyMap()); }
    public void success(CommandSender sender, String template, Map<String, String> values) {
        send(sender, template, values);
        sound(sender, config.get().sounds.success);
    }
    public void success(CommandSender sender, String template) { success(sender, template, Collections.emptyMap()); }
    public void error(CommandSender sender, String template, Map<String, String> values) {
        send(sender, template, values);
        sound(sender, config.get().sounds.error);
    }
    public void error(CommandSender sender, String template) { error(sender, template, Collections.emptyMap()); }
    private void sound(CommandSender sender, WalletConfig.SoundCue cue) {
        if (!(sender instanceof Player) || !config.get().sounds.enabled || cue.sound.isEmpty()) return;
        Player player = (Player) sender;
        player.playSound(player.getLocation(), cue.sound, cue.volume, cue.pitch);
    }
    public void failure(CommandSender sender, Throwable error) {
        if (error instanceof WalletException) {
            WalletException exception = (WalletException) error;
            WalletConfig.Messages messages = config.get().messages;
            String template;
            switch (exception.key) {
                case "unknownPlayer": template = messages.unknownPlayer; break;
                case "invalidAmount": template = messages.invalidAmount; break;
                case "insufficient": template = messages.insufficient; break;
                case "rewardCooldown": template = messages.rewardCooldown; break;
                case "rewardDisabled": template = messages.rewardDisabled; break;
                case "unavailable": template = messages.unavailable; break;
                case "busy": template = messages.busy; break;
                default: template = messages.error;
            }
            error(sender, template, exception.values);
        } else {
            logger.error("OPERACJA", "Operacja portfela nie została zakończona.", error);
            error(sender, config.get().messages.error);
        }
    }
}
