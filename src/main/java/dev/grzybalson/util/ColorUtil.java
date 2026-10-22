package dev.grzybalson.util;

import org.bukkit.ChatColor;
import java.util.regex.*;

public final class ColorUtil {
    private static final Pattern HEX = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private ColorUtil() {}

    public static String color(String input) {
        Matcher matcher = HEX.matcher(input == null ? "" : input);
        StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder value = new StringBuilder("§x");
            for (char c : hex.toCharArray()) value.append('§').append(c);
            matcher.appendReplacement(out, Matcher.quoteReplacement(value.toString()));
        }
        matcher.appendTail(out);
        return ChatColor.translateAlternateColorCodes('&', out.toString());
    }

}
