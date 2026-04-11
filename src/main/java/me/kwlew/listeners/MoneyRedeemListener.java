package me.kwlew.listeners;

import me.kwlew.api.EconomyService;
import me.kwlew.config.MessageManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class MoneyRedeemListener implements Listener {
    private final EconomyService economy;
    private final NamespacedKey key;
    private final MessageManager messages;

    public MoneyRedeemListener(EconomyService economy, Plugin plugin, MessageManager messages) {
        this.economy = economy;
        this.key = new NamespacedKey(plugin, "money_value");
        this.messages = messages;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getItem() == null) return;

        switch (event.getAction()) {
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {}
            default -> { return; }
        }

        ItemStack item = event.getItem();

        if (item.getType() != Material.PAPER) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        Double value = meta.getPersistentDataContainer().get(key, PersistentDataType.DOUBLE);
        if (value == null) return; // not money note

        Player player = event.getPlayer();

        event.setCancelled(true);

        if (player.isSneaking()) {
            int amount = item.getAmount();
            double total = value * amount;

            economy.addBalance(player.getUniqueId(), total);

            item.setAmount(0);

            String formatted = me.kwlew.utils.MoneyFormatter.format(total, "$");

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            player.sendMessage(messages.getWithPrefix("check-redeem",
                    "%amount%", formatted));
            return;
        }

        economy.addBalance(player.getUniqueId(), value);

        item.setAmount(item.getAmount()-1);

        String formatted = me.kwlew.utils.MoneyFormatter.format(value, "$");

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        player.sendMessage(messages.getWithPrefix("check-redeem",
                "%amount%", formatted));
    }
}