package me.kwlew.commands.subcommands;

import me.kwlew.api.EconomyService;
import me.kwlew.commands.SubCommand;
import me.kwlew.config.ConfigManager;
import me.kwlew.config.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PayCommand implements SubCommand {

    private final EconomyService economy;
    private final MessageManager messages;
    private final ConfigManager config;

    public PayCommand(EconomyService economy, MessageManager messages, ConfigManager config) {
        this.economy = economy;
        this.messages = messages;
        this.config = config;
    }


    @Override
    public String getName() {
        return "pay";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.getWithPrefix("no-player"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(messages.getWithPrefix("invalid-usage-pay"));
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);

        if (target == null || target.equals(player)) {
            sender.sendMessage(messages.getWithPrefix("no-player"));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(messages.getWithPrefix("invalid-number"));
            throw new RuntimeException(e);
        }

        if (amount <= 0) {
            sender.sendMessage(messages.getWithPrefix("invalid-number"));
            return;
        }

        double senderBalance = economy.getBalance(player.getUniqueId());

        if (senderBalance < amount) {
            sender.sendMessage(messages.getWithPrefix("not-enough-money"));
            return;
        }

        economy.removeBalance(player.getUniqueId(), amount);
        economy.addBalance(target.getUniqueId(), amount);

        String symbol = config.getCurrencySymbol();
        String formatted = me.kwlew.utils.MoneyFormatter.format(amount, symbol);

        sender.sendMessage(messages.getWithPrefix("pay-sent",
                "%amount", formatted,
                "%player", target.getName()
        ));

        sender.sendMessage(messages.getWithPrefix("pay-received",
                "%amount", formatted,
                "%player", target.getName()
        ));
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
