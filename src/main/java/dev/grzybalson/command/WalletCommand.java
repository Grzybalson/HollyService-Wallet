package dev.grzybalson.command;

import dev.grzybalson.gui.WalletMenus;
import dev.grzybalson.permission.WalletPermissions;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import dev.rollczi.litecommands.annotations.context.Sender;
import org.bukkit.entity.Player;

@Command(name = "portfel")
@Permission(WalletPermissions.USE)
public final class WalletCommand {
    private final WalletMenus menus;
    public WalletCommand(WalletMenus menus) { this.menus = menus; }
    @Execute public void open(@Sender Player player) { menus.main(player); }
}
