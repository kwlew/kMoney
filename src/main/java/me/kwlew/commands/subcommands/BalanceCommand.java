package me.kwlew.commands.subcommands;

import me.kwlew.api.EconomyService;
import me.kwlew.commands.SubCommand;
import me.kwlew.config.ConfigManager;
import me.kwlew.config.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BalanceCommand implements SubCommand {

    private final EconomyService economy;
    private final MessageManager messages;
    private final ConfigManager config;

    public BalanceCommand(EconomyService economy, MessageManager messages, ConfigManager config) {
        this.economy = economy;
        this.messages = messages;
        this.config = config;
    }

    @Override
    public String getName() {
        return "balance";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        // /money balance
        if (args.length == 0) {

            if (!(sender instanceof Player player)) {
                sender.sendMessage(messages.getWithPrefix("no-player"));
                return;
            }

            double balance = economy.getBalance(player.getUniqueId());
            String symbol = config.getCurrencySymbol();
            String formatted = me.kwlew.utils.MoneyFormatter.format(balance, symbol);

            player.sendMessage(
                    messages.getWithPrefix("balance",
                            "%balance%", formatted
                    )
            );

            return;
        }

        // /money balance <player>
        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(messages.getWithPrefix("no-player"));
            return;
        }

        double balance = economy.getBalance(target.getUniqueId());
        String symbol = config.getCurrencySymbol();

        if (sender instanceof Player player && args[0].equalsIgnoreCase(player.getName())) {
            sender.sendMessage(
                    messages.getWithPrefix("balance",
                            "%balance%", me.kwlew.utils.MoneyFormatter.format(balance, symbol)
                    )
            );
            return;
        }

        sender.sendMessage(
                messages.getWithPrefix("other-balance",
                        "%player%", target.getName(),
                        "%balance%", me.kwlew.utils.MoneyFormatter.format(balance, symbol)
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
