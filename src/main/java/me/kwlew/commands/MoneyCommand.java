package me.kwlew.commands;

import me.kwlew.api.EconomyService;
import me.kwlew.commands.subcommands.*;
import me.kwlew.config.ConfigManager;
import me.kwlew.config.MessageManager;
import me.kwlew.kMoney;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class MoneyCommand implements CommandExecutor, TabCompleter {

    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public MoneyCommand(EconomyService economy,
                        MessageManager messages,
                        ConfigManager config,
                        kMoney plugin) {

        register(new BalanceCommand(economy, messages, config));
        register(new AddCommand(economy, messages, config));
        register(new RemoveCommand(economy, messages, config));
        register(new SetCommand(economy, messages, config));
        register(new ReloadCommand(config, messages));
        register(new PayCommand(economy, messages, config));
        register(new WithdrawCommand(economy, messages, config, plugin));
        register(new MoneyAdminCommand(plugin, messages));
    }

    private void register(SubCommand sub) {
        subCommands.put(sub.getName().toLowerCase(), sub);
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender,
                                      @NonNull Command command,
                                      @NonNull String alias,
                                      String[] args) {

        if (args.length == 1 && sender.hasPermission("kmoney.admin")) {
            return new ArrayList<>(subCommands.keySet());
        }

        SubCommand sub = subCommands.get(args[0].toLowerCase());

        if (sub == null) return List.of();

        String[] subArgs = java.util.Arrays.copyOfRange(args, 1, args.length);
        return sub.tabComplete(sender, subArgs);
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender,
                             @NonNull Command command,
                             @NonNull String label,
                             String[] args) {

        if (args.length == 0) {

            SubCommand balance = subCommands.get("balance");

            if (balance != null) {
                balance.execute(sender, new String[0]);
            }

            return true;
        }

        SubCommand sub = subCommands.get(args[0].toLowerCase());

        if (sub == null) {
            sender.sendMessage(Component.text("Unknown subcommand.", NamedTextColor.RED));
            return true;
        }

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        sub.execute(sender, subArgs);
        return true;
    }
}
