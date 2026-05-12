package dev.kwlew.kmoney.listeners.craft;

import dev.kwlew.kmoney.kernel.Inject;
import dev.kwlew.kmoney.kernel.LifecycleComponent;
import dev.kwlew.kmoney.listeners.ListenerComponent;
import dev.kwlew.kmoney.managers.utils.MoneyCheckUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class HopperListener implements ListenerComponent, LifecycleComponent {

    private final JavaPlugin plugin;

    @Inject
    public HopperListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onHopperMove(InventoryMoveItemEvent event) {
        InventoryType type = event.getDestination().getType();
        ItemStack item = event.getItem();

        if (type == InventoryType.CRAFTER && MoneyCheckUtil.isMoneyCheck(item)) {
            event.setCancelled(true);
        }
    }
}
