package dev.grzybalson.logging;

import dev.grzybalson.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WalletLogger {
    private static final String PREFIX = "&8[&#38BDF8Wallet&8] ";
    private final Logger logger;
    private final String version;

    public WalletLogger(JavaPlugin plugin) {
        this.logger = plugin.getLogger();
        this.version = plugin.getDescription().getVersion();
    }

    public void ready(String database, int rows, int categories, long millis) {
        line("&8┌ &#38BDF8&lHOLLY&f&lSERVICE &8/ &fWallet &8v&7" + version);
        line("&8│ &a● &fONLINE &8· &7" + database + " &8· &7" + rows + " rzędów");
        line("&8└ &7Kategorie &f" + categories + " &8· &7Start &f" + millis + " ms &8· &#38BDF8/portfel");
    }

    public void info(String event, String message) {
        line(PREFIX + "&8[&7" + event + "&8] &f" + message);
    }

    public void warn(String event, String message) {
        line(PREFIX + "&8[&e" + event + "&8] &e" + message);
    }

    public void error(String event, String message, Throwable cause) {
        line(PREFIX + "&8[&c" + event + "&8] &f" + message);
        logger.log(Level.SEVERE, "Szczegóły zdarzenia [" + event + "]", cause);
    }

    private void line(String message) {
        Bukkit.getConsoleSender().sendMessage(ColorUtil.color(message));
    }
}
