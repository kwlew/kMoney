package me.kwlew.commands;

import me.kwlew.commands.base.BaseCommand;
import me.kwlew.kMoney;
import me.kwlew.managers.MessageManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.TabCompleter;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public class MoneyCommand extends BaseCommand implements TabCompleter {

    public MoneyCommand(kMoney plugin) {
        super(plugin);
    }

    @Override
    public java.util.List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, String[] args) {
        java.util.List<String> list = new java.util.ArrayList<>();

        if (args.length == 1) {
            list.add("balance");
            if (sender.hasPermission("money.admin.add")) {
                list.add("add");
            }
            if (sender.hasPermission("money.admin.set")){
                list.add("set");
            }
            if (sender.hasPermission("money.admin.reload")){
                list.add("reload");
            }
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("set"))) {
            for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
                list.add(player.getName());
            }
        }
        return list;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getMessageManager().get("only-players"));
                return true;
            }

            UUID uuid = player.getUniqueId();
            int money = plugin.getMoneyManager().getMoney(uuid);

            sender.sendMessage(
                    plugin.getMessageManager().get(
                            "balance",
                            "${money}", me.kwlew.utils.MoneyFormatter.format(money)
                    )
            );
            return true;
        }

        if (args[0].equalsIgnoreCase("balance")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getMessageManager().get("only-players"));
                return true;
            }

            UUID uuid = player.getUniqueId();
            int money = plugin.getMoneyManager().getMoney(uuid);

            sender.sendMessage(
                    plugin.getMessageManager().get(
                            "balance",
                            "${money}", me.kwlew.utils.MoneyFormatter.format(money)
                    )
            );
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {

            if (!sender.hasPermission("money.admin")) {
                sendError(sender, "No permission!");
                return true;
            }

            plugin.reloadConfig();
            plugin.setMessageManager(new MessageManager(plugin));

            sender.sendMessage(plugin.getMessageManager().get("reloaded"));
            plugin.sayReload();
            return true;
        }

        if (args[0].equalsIgnoreCase("add")) {

            if (args.length != 3) {
                sendError(sender, "Usage: /money add <player> <amount>");
                return true;
            }

            if (!sender.hasPermission("money.admin")) {
                sender.sendMessage(plugin.getMessageManager().get("no-permission"));
                return true;
            }

            Player target = getTarget(sender, args[1]);
            if (target == null) return true;

            Integer amount = parseAmount(sender, args[2]);
            if (amount == null) return true;

            UUID uuid = target.getUniqueId();

            plugin.getMoneyManager().addMoney(uuid, amount);
            plugin.getMoneyManager().savePlayer(uuid);

            sender.sendMessage(
                    plugin.getMessageManager().get(
                            "add-success",
                            "${amount}", me.kwlew.utils.MoneyFormatter.format(amount),
                            "${player}", target.getName()
                    )
            );

            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {

            if (args.length != 3) {
                sendError(sender, "Usage: /money set <player> <amount>");
                return true;
            }

            if (!sender.hasPermission("money.admin")) {
                sendError(sender, "No permission!");
                return true;
            }

            Player target = getTarget(sender, args[1]);
            if (target == null) return true;

            Integer amount = parseAmount(sender, args[2]);
            if (amount == null) return true;

            UUID uuid = target.getUniqueId();

            plugin.getMoneyManager().setMoney(uuid, amount);
            plugin.getMoneyManager().savePlayer(uuid);

            sender.sendMessage(
                    plugin.getMessageManager().get(
                            "set-success",
                            "${amount}", me.kwlew.utils.MoneyFormatter.format(amount),
                            "${player}", target.getName()
                    )
            );

            return true;
        }

        sendError(sender, "Unknown command!");
        return true;
    }
}