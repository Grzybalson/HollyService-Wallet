package dev.grzybalson.command;

import dev.grzybalson.service.WalletActions;
import dev.grzybalson.permission.WalletPermissions;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import dev.rollczi.litecommands.annotations.context.Sender;
import dev.rollczi.litecommands.annotations.argument.Arg;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.function.Consumer;

@Command(name = "aportfel")
public final class AdminWalletCommand {
    private final WalletActions actions;
    private final Consumer<CommandSender> reload;
    public AdminWalletCommand(WalletActions actions, Consumer<CommandSender> reload) { this.actions = actions; this.reload = reload; }
    @Execute(name = "add") @Permission(WalletPermissions.ADD)
    public void add(@Sender CommandSender sender, @Arg("nick") String name, @Arg("ilosc") String amount) { actions.modify(sender, "ADD", name, amount); }
    @Execute(name = "set") @Permission(WalletPermissions.SET)
    public void set(@Sender CommandSender sender, @Arg("nick") String name, @Arg("ilosc") String amount) { actions.modify(sender, "SET", name, amount); }
    @Execute(name = "remove") @Permission(WalletPermissions.REMOVE)
    public void remove(@Sender CommandSender sender, @Arg("nick") String name, @Arg("ilosc") String amount) { actions.modify(sender, "REMOVE", name, amount); }
    @Execute(name = "doladowanie") @Permission(WalletPermissions.TOPUP)
    public void topup(@Sender CommandSender sender, @Arg("nick") String name, @Arg("ilosc") String amount) { actions.modify(sender, "TOPUP", name, amount); }
    @Execute(name = "logs") @Permission(WalletPermissions.LOGS)
    public void logs(@Sender Player player, @Arg("nick") String name) { actions.logs(player, name); }
    @Execute(name = "reload") @Permission(WalletPermissions.RELOAD)
    public void reload(@Sender CommandSender sender) { reload.accept(sender); }
}
