package me.kwlew.commands.base;

import me.kwlew.kMoney;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class BaseCommand implements CommandExecutor {

    protected final kMoney plugin;

    public BaseCommand(kMoney plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (isPlayerOnly() && !(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command!", NamedTextColor.RED));
            return true;
        }

        return execute(sender, args);
    }

    public abstract boolean execute(CommandSender sender, String[] args);

    public boolean isPlayerOnly() {
        return true;
    }


    protected Player getPlayer(CommandSender sender) {
        return (Player) sender;
    }

    protected void sendError(CommandSender sender, String msg) {
        sender.sendMessage(Component.text(msg, NamedTextColor.RED));
    }

    protected void sendSuccess(CommandSender sender, String msg) {
        sender.sendMessage(Component.text(msg, NamedTextColor.GREEN));
    }

    protected Player getTarget(CommandSender sender, String name) {
        Player target = org.bukkit.Bukkit.getPlayer(name);

        if (target == null) {
            sendError(sender, "Player not found!");
        }

        return target;
    }

    protected Integer parseAmount(CommandSender sender, String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            sendError(sender, "Invalid number!");
            return null;
        }
    }
}