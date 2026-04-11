package me.kwlew.listeners;

import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class CraftListener implements Listener {

    private final NamespacedKey key;

    public CraftListener(Plugin plugin) {
        this.key = new NamespacedKey(plugin, "money_value");
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (item == null || item.getType() != Material.PAPER) continue;

            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            Double value = meta.getPersistentDataContainer()
                    .get(key, PersistentDataType.DOUBLE);

            if (value != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (item == null) continue;

            if (item.getType() != Material.PAPER) continue;

            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            Double value = meta.getPersistentDataContainer()
                    .get(key, PersistentDataType.DOUBLE);

            if (value != null) {
                event.getInventory().setResult(null); // 🔥 hides result
                return;
            }
        }
    }
}
