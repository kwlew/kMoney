package dev.kwlew.listeners;

import dev.kwlew.kernel.Inject;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class CraftListener implements ListenerComponent {

    private final NamespacedKey key;
    private final JavaPlugin plugin;

    @Inject
    public CraftListener(JavaPlugin plugin) {
        this.key = new NamespacedKey(plugin, "money");
        this.plugin = plugin;
    }

    @Override
    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (isMoneyCheck(item)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (isMoneyCheck(item)) {
                event.getInventory().setResult(null);
                return;
            }
        }
    }

    private boolean isMoneyCheck(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        String encoded = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (encoded != null) {
            return true;
        }

        Double legacy = meta.getPersistentDataContainer().get(key, PersistentDataType.DOUBLE);
        return legacy != null;
    }
}

