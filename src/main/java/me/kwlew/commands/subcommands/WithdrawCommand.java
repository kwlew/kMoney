package me.kwlew.commands.subcommands;

import me.kwlew.api.EconomyService;
import me.kwlew.commands.SubCommand;
import me.kwlew.config.ConfigManager;
import me.kwlew.config.MessageManager;
import me.kwlew.utils.MoneyParser;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.List;

public class WithdrawCommand implements SubCommand {

    private final EconomyService economy;
    private final MessageManager messages;
    private final ConfigManager config;
    private final NamespacedKey key;

    public WithdrawCommand(EconomyService economy, MessageManager messages, ConfigManager config, org.bukkit.plugin.Plugin plugin) {
        this.economy = economy;
        this.messages = messages;
        this.config = config;
        this.key = new NamespacedKey(plugin, "money_value");
    }

    @Override
    public String getName() {
        return "withdraw";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return;
        }

        if (args.length < 1) {
            player.sendMessage("Usage: /money withdraw <amount>");
            return;
        }

        double amount;

        try {
            amount = MoneyParser.parse(args[0]);
        } catch (Exception e) {
            player.sendMessage("Invalid amount.");
            return;
        }

        if (amount <= 0) {
            player.sendMessage("Invalid amount.");
            return;
        }

        double balance = economy.getBalance(player.getUniqueId());

        if (balance < amount) {
            player.sendMessage("Not enough money.");
            return;
        }

        economy.removeBalance(player.getUniqueId(), amount);

        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();

        String symbol = config.getCurrencySymbol();
        String formatted = me.kwlew.utils.MoneyFormatter.format(amount, symbol);

// NAME
        meta.displayName(
                messages.get("check-create-name")
                        .decoration(TextDecoration.ITALIC, false)
        );

// LORE
        meta.lore(java.util.List.of(
                messages.get("check-create-value", "%amount%", formatted)
                        .decoration(TextDecoration.ITALIC, false),
                messages.get("check-create-creator", "%player%", player.getName())
                        .decoration(TextDecoration.ITALIC, false)
        ));

        meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, amount);

        paper.setItemMeta(meta);

        player.getInventory().addItem(paper);

        player.sendMessage(messages.getWithPrefix("check-withdraw", "%amount%", formatted));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of("100", "1k", "10k", "1m");
    }
}