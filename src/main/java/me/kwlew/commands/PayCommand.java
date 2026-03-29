package me.kwlew.commands;

import me.kwlew.commands.base.BaseCommand;
import me.kwlew.kMoney;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.TabCompleter;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public class PayCommand extends BaseCommand implements TabCompleter {

    public PayCommand (kMoney plugin) {
        super(plugin);
    }

    public java.util.List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, String[] args) {
        java.util.List<String> list = new java.util.ArrayList<>();

        if (args.length == 1) {
            for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
                list.add(player.getName());
            }
        }
        return list;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2 || args.length > 3) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getMessageManager().get("only-players"));
                return true;
            }
            sendError(sender, "Usage: /pay <nick> <amount>");
            return true;
        }

        Player player = (Player) sender;

        Player target = getTarget(sender, args[0]);
        if (target == null) return true;

        Integer amount = parseAmount(sender, args[1]);
        if (amount == null || plugin.getMoneyManager().isNumberInvalid(amount)){
            sendError(sender, "Invalid number! Only positive numbers are allowed!");
            return true;
        }

        UUID receiver = target.getUniqueId();
        UUID payer = player.getUniqueId();

        if (receiver==payer){
            sendError(sender, "You cannot send money to yourself!");
            return true;
        }

        if (!plugin.getMoneyManager().canPay(amount, payer)) {
            sendError(sender, "You don't have enough money!");
            return true;
        }

        plugin.getMoneyManager().removeMoney(payer, amount);
        plugin.getMoneyManager().addMoney(receiver, amount);

        sender.sendMessage(plugin.getMessageManager().get(
                "player-pay",
                "${amount}", me.kwlew.utils.MoneyFormatter.format(amount),
                "${player}", player.getName()
        ));

        target.sendMessage(plugin.getMessageManager().get(
                "player-paid",
                "${amount}", me.kwlew.utils.MoneyFormatter.format(amount),
                "${player}", target.getName()
        ));

        return true;
    }
}
