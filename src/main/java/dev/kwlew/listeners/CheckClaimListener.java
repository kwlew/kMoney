package dev.kwlew.listeners;

import dev.kwlew.economy.EconomyManager;
import dev.kwlew.economy.utils.Formatter;
import dev.kwlew.kernel.Inject;
import dev.kwlew.managers.MessageManager;
import dev.kwlew.managers.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class CheckClaimListener implements ListenerComponent {

    private final NamespacedKey key;
    private final JavaPlugin plugin;
    private final EconomyManager economy;
    private final ConfigManager config;
    private final MessageManager messages;

    @Inject
    public CheckClaimListener(JavaPlugin plugin, EconomyManager economy, ConfigManager config, MessageManager messages) {
        this.key = new NamespacedKey(plugin, "money");
        this.plugin = plugin;
        this.economy = economy;
        this.config = config;
        this.messages = messages;
    }

    @Override
    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getItem() == null) return;

        switch(event.getAction()) {
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {}
            default -> { return; }
        }

        ItemStack item = event.getItem();
        if (item.getType() != Material.PAPER) return;

        if (item.getItemMeta() == null) return;
        ItemMeta meta = item.getItemMeta();

        Double value = meta.getPersistentDataContainer().get(key, PersistentDataType.DOUBLE);
        if (value == null) return;

        Player player = event.getPlayer();

        event.setCancelled(true);

        if (player.isSneaking()) {
            int amount = item.getAmount();
            double total = value * amount;

            economy.addBalance(player.getUniqueId(), total);

            item.setAmount(0);

            String formatted = Formatter.format(total, config.getCurrencySymbol());

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            messages.send(player, "check-redeem",
                    messages.placeholder("amount", formatted));
            return;
        }

        economy.addBalance(player.getUniqueId(), value);

        item.setAmount(item.getAmount()-1);

        String formatted = Formatter.format(value, config.getCurrencySymbol());

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

        messages.send(player, "check-redeem",
                messages.placeholder("amount", formatted));

    }
}
