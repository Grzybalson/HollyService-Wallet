package dev.grzybalson.gui;

import dev.grzybalson.config.*;
import dev.grzybalson.model.*;
import dev.grzybalson.permission.WalletPermissions;
import dev.grzybalson.service.*;
import dev.grzybalson.util.*;
import dev.triumphteam.gui.guis.*;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.InventoryHolder;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.*;

public final class WalletMenus implements Listener {
    private final Supplier<WalletConfig> config;
    private final WalletService service;
    private final TaskUtil tasks;
    private final MessageService messages;
    private final Consumer<Player> daily;
    private final BiConsumer<Player, PurchaseRequest> purchase;
    private final Map<UUID, Long> requests = new HashMap<>();
    private final Map<UUID, Gui> visible = new HashMap<>();

    public WalletMenus(Supplier<WalletConfig> config, WalletService service, TaskUtil tasks, MessageService messages,
                       Consumer<Player> daily, BiConsumer<Player, PurchaseRequest> purchase) {
        this.config = config; this.service = service; this.tasks = tasks; this.messages = messages; this.daily = daily; this.purchase = purchase;
    }

    public void main(Player player) { load(player, account -> main(player, account)); }
    private void main(Player player, Account account) {
        WalletConfig settings = config.get();
        Map<String, String> values = values(account);
        Gui gui = base(settings.title, values);
        put(gui, settings.dailyItem, values, () -> daily.accept(player));
        put(gui, settings.informationItem, values, null);
        settings.categories.forEach((id, category) -> put(gui, category.item, values, () -> category(player, id)));
        show(player, gui);
    }

    public void category(Player player, String id) {
        load(player, account -> {
            WalletConfig settings = config.get();
            WalletConfig.Category category = settings.categories.get(id);
            if (category == null) { main(player, account); return; }
            Map<String, String> values = values(account);
            Gui gui = base(category.title, values);
            category.products.forEach((productId, product) -> {
                Map<String, String> productValues = new HashMap<>(values);
                productValues.put("holly_cost", MoneyUtil.format(MoneyUtil.parse(product.price)));
                put(gui, product.item, productValues, () -> confirmation(player, id, productId));
            });
            put(gui, settings.backItem, values, () -> main(player));
            show(player, gui);
        });
    }

    private void confirmation(Player player, String categoryId, String productId) {
        load(player, account -> {
            WalletConfig settings = config.get();
            WalletConfig.Category category = settings.categories.get(categoryId);
            WalletConfig.Product product = category == null ? null : category.products.get(productId);
            if (product == null) { main(player, account); return; }
            PurchaseRequest request = new PurchaseRequest(categoryId, productId, product);
            Map<String, String> values = values(account);
            values.put("holly_cost", MoneyUtil.format(request.price));
            Gui gui = base(settings.confirmation.title, values);
            gui.setItem(settings.confirmation.previewSlot, new GuiItem(ItemUtil.create(product.item, values, settings.hideAttributes)));
            AtomicBoolean selected = new AtomicBoolean();
            put(gui, settings.confirmation.confirm, values, () -> {
                if (selected.compareAndSet(false, true)) { player.closeInventory(); purchase.accept(player, request); }
            });
            put(gui, settings.confirmation.cancel, values, () -> category(player, categoryId));
            put(gui, settings.backItem, values, () -> main(player));
            show(player, gui);
        });
    }

    public void logs(Player viewer, String name, PlayerIdentity known, int page) {
        long token = next(viewer);
        int size = config.get().logs.slots.size();
        tasks.submit(() -> {
            PlayerIdentity identity = service.resolve(name, known);
            return new LogPage(identity, service.logs(identity.id, page, size));
        }, result -> {
            if (!valid(viewer, token)) return;
            if (!viewer.hasPermission(WalletPermissions.LOGS)) { messages.error(viewer, config.get().messages.noPermission); return; }
            WalletConfig settings = config.get();
            Map<String, String> values = PlaceholderUtil.values("player", result.identity.name, "page", Integer.toString(page + 1));
            Gui gui = base(settings.logs.title, values);
            for (int i = 0; i < Math.min(size, result.logs.size()); i++) {
                WalletLog log = result.logs.get(i);
                Map<String, String> entryValues = new HashMap<>(values);
                entryValues.putAll(PlaceholderUtil.values("type", type(log.type), "product", log.product, "date", TimeUtil.date(log.timestamp, settings.timezone),
                    "amount", MoneyUtil.format(log.amount), "balance", MoneyUtil.format(log.balance), "actor", log.actor, "status", status(log.status), "id", log.id));
                gui.setItem(settings.logs.slots.get(i), new GuiItem(ItemUtil.create(settings.logs.entry, entryValues, settings.hideAttributes)));
            }
            if (result.logs.isEmpty()) put(gui, settings.logs.empty, values, null);
            if (page > 0) put(gui, settings.logs.previous, values, () -> logs(viewer, result.identity.name, result.identity, page - 1));
            if (result.logs.size() > size) put(gui, settings.logs.next, values, () -> logs(viewer, result.identity.name, result.identity, page + 1));
            put(gui, settings.backItem, values, () -> main(viewer));
            show(viewer, gui);
        }, exception -> { if (valid(viewer, token)) messages.failure(viewer, exception); });
    }

    private void load(Player player, Consumer<Account> render) {
        long token = next(player);
        PlayerIdentity identity = new PlayerIdentity(player.getUniqueId(), player.getName());
        tasks.submit(() -> service.load(identity), account -> {
            if (!valid(player, token)) return;
            if (!player.hasPermission(WalletPermissions.USE)) { messages.error(player, config.get().messages.noPermission); return; }
            render.accept(account);
        }, exception -> { if (valid(player, token)) messages.failure(player, exception); });
    }

    private Gui base(String title, Map<String, String> values) {
        WalletConfig settings = config.get();
        Gui gui = Gui.gui().rows(settings.rows)
            .title(LegacyComponentSerializer.legacySection().deserialize(ColorUtil.color(PlaceholderUtil.replace(title, values))))
            .disableAllInteractions().create();
        gui.setDefaultClickAction(event -> event.setCancelled(true));
        gui.setDragAction(event -> event.setCancelled(true));
        settings.template54.forEach((material, slots) -> {
            ItemConfig decoration = new ItemConfig(0, material, settings.backgroundItemName);
            decoration.customModelData = settings.backgroundCustomModelData.getOrDefault(material, -1);
            for (int slot : slots) gui.setItem(SlotUtil.template(slot, settings.rows), new GuiItem(ItemUtil.create(decoration, values, settings.hideAttributes)));
        });
        return gui;
    }

    private void put(Gui gui, ItemConfig item, Map<String, String> values, Runnable action) {
        gui.setItem(item.slot, new GuiItem(ItemUtil.create(item, values, config.get().hideAttributes), event -> {
            event.setCancelled(true);
            if (action != null && (event.getClick() == ClickType.LEFT || event.getClick() == ClickType.RIGHT)) tasks.later(() -> {
                if (visible.get(event.getWhoClicked().getUniqueId()) == gui) action.run();
            });
        }));
    }

    private Map<String, String> values(Account account) {
        WalletConfig settings = config.get();
        return PlaceholderUtil.values("player", account.name, "hollywallet_wpln", MoneyUtil.format(account.balance),
            "reward_min", MoneyUtil.format(MoneyUtil.parse(settings.reward.min)), "reward_max", MoneyUtil.format(MoneyUtil.parse(settings.reward.max)));
    }
    private long next(Player player) { return requests.merge(player.getUniqueId(), 1L, Long::sum); }
    private boolean valid(Player player, long token) { return player.isOnline() && Objects.equals(requests.get(player.getUniqueId()), token); }
    private void show(Player player, Gui gui) { visible.put(player.getUniqueId(), gui); gui.open(player); }

    public InventoryHolder current(Player player) { return visible.get(player.getUniqueId()); }
    public void refresh(Player player, InventoryHolder previous) {
        if (previous != null && visible.get(player.getUniqueId()) == previous) main(player);
    }

    @EventHandler public void close(InventoryCloseEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        Gui current = visible.get(id);
        if (current != null && event.getInventory().getHolder() == current) { visible.remove(id); requests.merge(id, 1L, Long::sum); }
    }
    public void forget(Player player) { visible.remove(player.getUniqueId()); requests.remove(player.getUniqueId()); }
    public void closeAll() {
        for (Gui gui : new ArrayList<>(visible.values())) for (org.bukkit.entity.HumanEntity player : new ArrayList<>(gui.getInventory().getViewers())) player.closeInventory();
        visible.clear(); requests.clear();
    }

    private static String type(String type) {
        switch (type) { case "PURCHASE": return "ᴢᴀᴋᴜᴘ"; case "REWARD": return "ɴᴀɢʀᴏᴅᴀ"; case "TOPUP": return "ᴅᴏʟᴀᴅᴏᴡᴀɴɪᴇ"; case "ADD": return "ᴅᴏᴅᴀɴɪᴇ"; case "SET": return "ᴜꜱᴛᴀᴡɪᴇɴɪᴇ"; case "REMOVE": return "ᴏᴅᴇᴊᴍᴏᴡᴀɴɪᴇ"; default: return type; }
    }
    private static String status(String status) {
        switch (status) { case "COMPLETED": return "&aᴢᴀᴋᴏɴᴄᴢᴏɴᴏ"; case "PENDING": return "&eᴡ ᴛᴏᴋᴜ"; case "REVIEW": return "&cᴅᴏ ꜱᴘʀᴀᴡᴅᴢᴇɴɪᴀ"; default: return status; }
    }
    private static final class LogPage {
        final PlayerIdentity identity;
        final List<WalletLog> logs;
        LogPage(PlayerIdentity identity, List<WalletLog> logs) { this.identity = identity; this.logs = logs; }
    }
}
