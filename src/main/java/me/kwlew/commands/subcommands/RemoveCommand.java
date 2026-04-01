package me.kwlew.commands.subcommands;

import me.kwlew.api.EconomyService;
import me.kwlew.commands.SubCommand;
import me.kwlew.config.ConfigManager;
import me.kwlew.config.MessageManager;
import me.kwlew.utils.MoneyParser;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class RemoveCommand implements SubCommand {

    private final EconomyService economy;
    private final MessageManager messages;
    private final ConfigManager config;

    public RemoveCommand(EconomyService economy, MessageManager messages, ConfigManager config) {
        this.economy = economy;
        this.messages = messages;
        this.config = config;
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        // /money remove <player> <amount>
        if (args.length < 2) {
            sender.sendMessage(messages.getWithPrefix("invalid-usage-remove"));
            return;
        }

        // Optional permission check
        if (!sender.hasPermission("kmoney.admin")) {
            sender.sendMessage(messages.getWithPrefix("no-permission"));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(messages.getWithPrefix("no-player"));
            return;
        }

        double amount;
        try {
            amount = MoneyParser.parse(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(messages.getWithPrefix("invalid-number"));
            return;
        }

        if (amount <= 0) {
            sender.sendMessage(messages.getWithPrefix("invalid-number"));
            return;
        }

        economy.removeBalance(target.getUniqueId(), amount);
        String symbol = config.getCurrencySymbol();
        String formatted = me.kwlew.utils.MoneyFormatter.format(amount, symbol);

        sender.sendMessage(
                messages.getWithPrefix("removed",
                        "%amount%", formatted,
                        "%player%", target.getName()
                )
        );
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {

        if (args.length == 1) {
            return Bukkit.getOnlinePlayers()
                    .stream()
                    .map(Player::getName)
                    .toList();
        }

        return List.of();
    }
}