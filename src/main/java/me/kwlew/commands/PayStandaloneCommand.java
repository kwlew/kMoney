package me.kwlew.commands;

import me.kwlew.commands.subcommands.PayCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class PayStandaloneCommand implements CommandExecutor {

    private final PayCommand payCommand;

    public PayStandaloneCommand(PayCommand payCommand) {
        this.payCommand = payCommand;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        payCommand.execute(sender, args);
        return true;
    }
}
