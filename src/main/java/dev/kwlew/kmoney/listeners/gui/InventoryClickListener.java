package dev.kwlew.kmoney.listeners.gui;

import dev.kwlew.kmoney.kernel.Inject;
import dev.kwlew.kmoney.kernel.LifecycleComponent;
import dev.kwlew.kmoney.listeners.ListenerComponent;
import dev.kwlew.kmoney.managers.check.CheckHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class InventoryClickListener implements LifecycleComponent, ListenerComponent {

    private final JavaPlugin plugin;

    @Inject
    public InventoryClickListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryType inventoryType = event.getInventory().getType();

        if (!CheckHandler.isBlacklistedInventory(inventoryType)) return;

        ItemStack item = event.getCurrentItem();

        if (item == null) return;

        if (CheckHandler.isMoneyCheck(item)) {
            event.setCancelled(true);
            return;
        }

        if (event.getClick() == ClickType.NUMBER_KEY) {
            int hotBarSlot = event.getHotbarButton();
            ItemStack hotbarItem = event.getWhoClicked().getInventory().getItem(hotBarSlot);

            if (CheckHandler.isMoneyCheck(hotbarItem)) {
                event.setCancelled(true);
            }
        }
    }
}
