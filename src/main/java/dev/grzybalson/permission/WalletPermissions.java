package dev.grzybalson.permission;

import org.bukkit.permissions.*;
import org.bukkit.plugin.PluginManager;
import java.util.*;

public final class WalletPermissions implements AutoCloseable {
    public static final String USE = "holly.cmd.wallet.use";
    public static final String ADD = "holly.cmd.wallet.add";
    public static final String SET = "holly.cmd.wallet.set";
    public static final String REMOVE = "holly.cmd.wallet.remove";
    public static final String TOPUP = "holly.cmd.wallet.doladowanie";
    public static final String LOGS = "holly.cmd.wallet.logs";
    public static final String RELOAD = "holly.cmd.wallet.reload";
    public static final String ADMIN = "holly.cmd.wallet.admin";
    public static final String ALL = "holly.cmd.wallet.*";
    private final PluginManager manager;
    private final List<Permission> registered = new ArrayList<>();

    public WalletPermissions(PluginManager manager) {
        this.manager = manager;
        add(USE, PermissionDefault.TRUE, Collections.emptyMap());
        Map<String, Boolean> admin = new LinkedHashMap<>();
        for (String name : Arrays.asList(ADD, SET, REMOVE, TOPUP, LOGS, RELOAD)) {
            add(name, PermissionDefault.OP, Collections.emptyMap());
            admin.put(name, true);
        }
        add(ADMIN, PermissionDefault.OP, admin);
        Map<String, Boolean> all = new LinkedHashMap<>();
        all.put(USE, true); all.put(ADMIN, true);
        add(ALL, PermissionDefault.OP, all);
    }
    private void add(String name, PermissionDefault defaults, Map<String, Boolean> children) {
        if (manager.getPermission(name) != null) return;
        Permission permission = new Permission(name, defaults, children);
        manager.addPermission(permission);
        registered.add(permission);
    }
    @Override public void close() {
        for (int i = registered.size() - 1; i >= 0; i--) manager.removePermission(registered.get(i));
        registered.clear();
    }
}
