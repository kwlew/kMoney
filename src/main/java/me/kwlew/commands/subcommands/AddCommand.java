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

public class AddCommand implements SubCommand {

    private final EconomyService economy;
    private final MessageManager messages;
    private final ConfigManager config;

    public AddCommand(EconomyService economy, MessageManager messages, ConfigManager config) {
        this.economy = economy;
        this.messages = messages;
        this.config = config;
    }

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (args.length < 2) {
            sender.sendMessage(messages.getWithPrefix("invalid-usage-add"));
            return;
        }

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

        economy.addBalance(target.getUniqueId(), amount);
        String symbol = config.getCurrencySymbol();
        String formatted = me.kwlew.utils.MoneyFormatter.format(amount, symbol);

        sender.sendMessage(
                messages.getWithPrefix("added",
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
