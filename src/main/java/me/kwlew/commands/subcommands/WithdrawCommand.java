package me.kwlew.commands.subcommands;

import me.kwlew.api.EconomyService;
import me.kwlew.commands.SubCommand;
import me.kwlew.config.ConfigManager;
import me.kwlew.config.MessageManager;
import me.kwlew.utils.MoneyParser;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
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
            player.sendMessage(messages.getWithPrefix("invalid-usage-withdraw"));
            return;
        }

        double amount;
        int notes;

        try {
            amount = MoneyParser.parse(args[0]);
        } catch (Exception e) {
            player.sendMessage(messages.getWithPrefix("invalid-number"));
            return;
        }
        if (args.length == 2) {
            try {
                notes = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(messages.getWithPrefix("invalid-number"));
                return;
            }

            if (notes > 64) {
                player.sendMessage(messages.getWithPrefix("invalid-number"));
                return;
            }
        }
        else {
            notes = 1;
        }

        if (amount <= 0) {
            player.sendMessage(messages.getWithPrefix("invalid-number"));
            return;
        }

        double balance = economy.getBalance(player.getUniqueId());

        if (balance < amount*notes) {
            player.sendMessage(messages.getWithPrefix("not-enough-money"));
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(messages.getWithPrefix("inventory-full"));
            return;
        }

        ItemStack paper = new ItemStack(Material.PAPER, notes);
        ItemMeta meta = paper.getItemMeta();

        String symbol = config.getCurrencySymbol();
        String formatted = me.kwlew.utils.MoneyFormatter.format(amount*notes, symbol);

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

        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
        player.getInventory().addItem(paper);
        economy.removeBalance(player.getUniqueId(), amount*notes);

        player.sendMessage(messages.getWithPrefix("check-withdraw", "%amount%", formatted));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("1K", "1M", "1B");
        }
        return List.of("1", "2", "4", "8", "16");
    }
}