package dev.kwlew.kmoney.listeners.player;

import dev.kwlew.kmoney.economy.EconomyManager;
import dev.kwlew.kmoney.economy.utils.Formatter;
import dev.kwlew.kmoney.kernel.Inject;
import dev.kwlew.kmoney.listeners.ListenerComponent;
import dev.kwlew.kmoney.managers.utils.MessageManager;
import dev.kwlew.kmoney.managers.config.ConfigManager;
import dev.kwlew.kmoney.managers.check.Check;
import dev.kwlew.kmoney.managers.sound.SoundFactory;
import dev.kwlew.kmoney.managers.sound.SoundType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;

public class CheckClaimListener implements ListenerComponent {

    private final NamespacedKey key;
    private final JavaPlugin plugin;
    private final EconomyManager economy;
    private final ConfigManager config;
    private final MessageManager messages;
    private final SoundFactory soundFactory;

    @Inject
    public CheckClaimListener(JavaPlugin plugin,
                              EconomyManager economy,
                              ConfigManager config,
                              MessageManager messages,
                              SoundFactory soundFactory) {
        this.key = new NamespacedKey(plugin, "money");
        this.plugin = plugin;
        this.economy = economy;
        this.config = config;
        this.messages = messages;
        this.soundFactory = soundFactory;
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
        if (item.getType() != Check.getMaterial()) return;

        if (item.getItemMeta() == null) return;
        ItemMeta meta = item.getItemMeta();

        BigDecimal value = getCheckValue(meta);
        if (value == null) return;

        Player player = event.getPlayer();

        event.setCancelled(true);

        if (player.isSneaking()) {
            int amount = item.getAmount();
            BigDecimal total = value.multiply(BigDecimal.valueOf(amount));

            economy.addBalance(player.getUniqueId(), total);

            item.setAmount(0);

            String formatted = Formatter.format(total, config.getCurrencySymbol());

            soundFactory.play(player, SoundType.REDEEM);
            messages.send(player, "check.redeem",
                    messages.placeholder("amount", formatted));
            return;
        }

        economy.addBalance(player.getUniqueId(), value);

        item.setAmount(item.getAmount()-1);

        String formatted = Formatter.format(value, config.getCurrencySymbol());

        soundFactory.play(player, SoundType.REDEEM);

        messages.send(player, "check.redeem",
                messages.placeholder("amount", formatted));

    }

    private BigDecimal getCheckValue(ItemMeta meta) {
        String encoded = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (encoded != null) {
            try {
                return new BigDecimal(encoded);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        Double legacy = meta.getPersistentDataContainer().get(key, PersistentDataType.DOUBLE);
        if (legacy != null) {
            return BigDecimal.valueOf(legacy);
        }

        return null;
    }
}