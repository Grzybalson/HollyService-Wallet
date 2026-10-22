package dev.grzybalson.config;

import dev.grzybalson.util.*;
import eu.okaeri.configs.*;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.ZoneId;
import java.util.*;

public final class ConfigLoader {
    private ConfigLoader() {}
    public static <T extends OkaeriConfig> T load(Class<T> type, File file) {
        T config = ConfigManager.create(type);
        config.withConfigurer(new YamlBukkitConfigurer()).withBindFile(file);
        try {
            if (Files.exists(file.toPath())) {
                String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                if (type == WalletConfig.class) content = content.replace("%holly_playerwpln%", "%hollywallet_wpln%");
                config.load(content);
            }
            save(config, file.toPath());
            return config;
        } catch (IOException exception) { throw new IllegalStateException("Nie udalo sie wczytac " + file.getName(), exception); }
    }

    public static void save(OkaeriConfig config, Path file) {
        try {
            Path absolute = file.toAbsolutePath();
            Files.createDirectories(absolute.getParent());
            Path temporary = Files.createTempFile(absolute.getParent(), file.getFileName().toString(), ".tmp");
            try {
                Files.write(temporary, config.saveToString().getBytes(StandardCharsets.UTF_8));
                try { Files.move(temporary, absolute, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
                catch (AtomicMoveNotSupportedException exception) { Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING); }
            } finally { Files.deleteIfExists(temporary); }
        } catch (IOException exception) { throw new IllegalStateException("Nie udalo sie zapisac " + file.getFileName(), exception); }
    }

    public static void validate(WalletConfig config) {
        if (config.rows < 5 || config.rows > 6) throw new IllegalArgumentException("rows: dozwolone 5 lub 6");
        ZoneId.of(config.timezone);
        for (Map.Entry<String, Integer> model : config.backgroundCustomModelData.entrySet()) {
            MaterialUtil.item(model.getKey());
            if (model.getValue() == null || model.getValue() < -1) throw new IllegalArgumentException("Niepoprawne CustomModelData dekoracji: " + model.getKey());
        }
        long min = MoneyUtil.parse(config.reward.min), max = MoneyUtil.parse(config.reward.max);
        if (min < 1 || max < min || config.reward.cooldownHours < 1 || config.reward.cooldownHours > 87600) throw new IllegalArgumentException("Niepoprawny zakres nagrody lub cooldown");
        Set<Integer> main = new HashSet<>();
        item(config.dailyItem, config.rows, "dailyItem"); reserve(main, config.dailyItem.slot);
        item(config.informationItem, config.rows, "informationItem"); reserve(main, config.informationItem.slot);
        item(config.backItem, config.rows, "backItem");
        for (Map.Entry<String, List<Integer>> decoration : config.template54.entrySet()) {
            MaterialUtil.item(decoration.getKey());
            for (int slot : decoration.getValue()) {
                if (slot < 0 || slot > 53) throw new IllegalArgumentException("template54: slot poza zakresem");
                SlotUtil.check(SlotUtil.template(slot, config.rows), config.rows, "template54");
            }
        }
        for (Map.Entry<String, WalletConfig.Category> entry : config.categories.entrySet()) {
            WalletConfig.Category category = entry.getValue();
            item(category.item, config.rows, "categories." + entry.getKey()); reserve(main, category.item.slot);
            Set<Integer> products = new HashSet<>(); products.add(config.backItem.slot);
            for (WalletConfig.Product product : category.products.values()) {
                item(product.item, config.rows, "products"); reserve(products, product.item.slot);
                if (MoneyUtil.parse(product.price) <= 0) throw new IllegalArgumentException("Cena produktu musi byc dodatnia");
                for (String command : product.commands) if (command == null || command.trim().isEmpty() || command.contains("\n") || command.contains("\r")) throw new IllegalArgumentException("Pusta lub wielowierszowa komenda produktu");
            }
        }
        Set<Integer> confirmation = new HashSet<>();
        item(config.confirmation.confirm, config.rows, "confirmation.confirm"); reserve(confirmation, config.confirmation.confirm.slot);
        item(config.confirmation.cancel, config.rows, "confirmation.cancel"); reserve(confirmation, config.confirmation.cancel.slot);
        SlotUtil.check(config.confirmation.previewSlot, config.rows, "confirmation.previewSlot"); reserve(confirmation, config.confirmation.previewSlot);
        reserve(confirmation, config.backItem.slot);
        if (config.logs.slots.isEmpty()) throw new IllegalArgumentException("Brak slotow historii");
        Set<Integer> logs = new HashSet<>();
        for (int slot : config.logs.slots) { SlotUtil.check(slot, config.rows, "logs.slots"); reserve(logs, slot); }
        item(config.logs.entry, config.rows, "logs.entry");
        item(config.logs.empty, config.rows, "logs.empty");
        item(config.logs.previous, config.rows, "logs.previous"); reserve(logs, config.logs.previous.slot);
        item(config.logs.next, config.rows, "logs.next"); reserve(logs, config.logs.next.slot);
        reserve(logs, config.backItem.slot);
        if (!Float.isFinite(config.topup.volume) || !Float.isFinite(config.topup.pitch) || config.topup.volume < 0 || config.topup.pitch <= 0) throw new IllegalArgumentException("Niepoprawne parametry dzwieku");
        if (config.topup.fadeIn < 0 || config.topup.stay < 0 || config.topup.fadeOut < 0) throw new IllegalArgumentException("Niepoprawny czas title");
        for (WalletConfig.SoundCue cue : Arrays.asList(config.sounds.success, config.sounds.error)) {
            if (cue.sound == null || !Float.isFinite(cue.volume) || !Float.isFinite(cue.pitch) || cue.volume < 0 || cue.pitch <= 0) throw new IllegalArgumentException("Niepoprawny dzwiek komunikatu");
        }
    }
    private static void reserve(Set<Integer> slots, int slot) {
        if (!slots.add(slot)) throw new IllegalArgumentException("Powtorzony slot interaktywny: " + slot);
    }
    private static void item(ItemConfig item, int rows, String path) {
        SlotUtil.check(item.slot, rows, path);
        MaterialUtil.item(item.material);
        if (item.customModelData < -1) throw new IllegalArgumentException("Niepoprawne CustomModelData: " + path);
        if (item.displayname == null || item.lore == null || item.lore.contains(null)) throw new IllegalArgumentException("Niepoprawny opis: " + path);
    }
}
