package dev.grzybalson.config;

import eu.okaeri.configs.OkaeriConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemConfig extends OkaeriConfig {
    public int slot = 0;
    public String material = "BOOK";
    public int customModelData = -1;
    public String displayname = "&fPrzedmiot";
    public List<String> lore = new ArrayList<>();

    public ItemConfig() {}

    public ItemConfig(int slot, String material, String name, String... lore) {
        this.slot = slot;
        this.material = material;
        this.displayname = name;
        this.lore = new ArrayList<>(Arrays.asList(lore));
    }
}
