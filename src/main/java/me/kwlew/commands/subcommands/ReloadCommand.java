package me.kwlew.commands.subcommands;

import me.kwlew.commands.SubCommand;
import me.kwlew.config.ConfigManager;
import me.kwlew.config.MessageManager;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ReloadCommand implements SubCommand {

    private final ConfigManager config;
    private final MessageManager messages;

    public ReloadCommand(ConfigManager config, MessageManager messages) {
        this.config = config;
        this.messages = messages;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (!sender.hasPermission("kmoney.admin")) {
            sender.sendMessage(messages.getWithPrefix("no-permission"));
            return;
        }

        sender.sendMessage(messages.getWithPrefix("reloading"));

        config.reloadAll();
        messages.reload();

        sender.sendMessage(messages.getWithPrefix("reloaded"));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}