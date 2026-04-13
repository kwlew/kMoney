package me.kwlew.commands.subcommands;

import me.kwlew.commands.SubCommand;
import me.kwlew.config.MessageManager;
import me.kwlew.kMoney;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class MoneyAdminCommand implements SubCommand {

    private final kMoney plugin;
    private final MessageManager messages;

    public MoneyAdminCommand(kMoney plugin, MessageManager messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public String getName() {
        return "admin";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.getWithPrefix("no-player"));
            return;
        }

        if (!sender.hasPermission("kmoney.admin")) {
            sender.sendMessage(messages.getWithPrefix("no-permission"));
            return;
        }

        UUID uuid = player.getUniqueId();

        if (args.length < 1) {
            player.sendMessage(messages.getWithPrefix("invalid-usage-admin"));
            return;
        }

        if (args[0].equalsIgnoreCase("off")) {
            plugin.getAdminMessageDisabled().add(uuid);
            player.sendMessage(messages.getWithPrefix("admin-off"));
            return;
        }

        if (args[0].equalsIgnoreCase("on")) {
            plugin.getAdminMessageDisabled().remove(uuid);
            player.sendMessage(messages.getWithPrefix("admin-on"));
            return;
        }

        player.sendMessage(messages.getWithPrefix("invalid-usage-admin"));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("on", "off");
        }
        return List.of();
    }
}
