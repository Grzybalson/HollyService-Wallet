package dev.grzybalson.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.*;

public final class MaterialUtil {
    private MaterialUtil() {}
    public static ItemStack item(String input) {
        String name = input.toUpperCase(Locale.ROOT).replace(' ', '_');
        Material material = Material.matchMaterial(name);
        if (material != null && material != Material.AIR) return new ItemStack(material);
        throw new IllegalArgumentException("Nieznany material: " + input);
    }
}
