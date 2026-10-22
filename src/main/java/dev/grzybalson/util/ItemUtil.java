package dev.grzybalson.util;

import dev.grzybalson.config.ItemConfig;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;

public final class ItemUtil {
    private ItemUtil() {}
    public static ItemStack create(ItemConfig config, Map<String, String> placeholders, boolean hide) {
        ItemStack item = MaterialUtil.item(config.material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) throw new IllegalArgumentException("Przedmiot bez metadanych: " + config.material);
        meta.setDisplayName(ColorUtil.color(PlaceholderUtil.replace(config.displayname, placeholders)));
        if (config.customModelData >= 0) meta.setCustomModelData(config.customModelData);
        List<String> lore = new ArrayList<>();
        for (String line : config.lore) lore.add(ColorUtil.color(PlaceholderUtil.replace(line, placeholders)));
        meta.setLore(lore);
        if (hide) meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        return item;
    }
}
