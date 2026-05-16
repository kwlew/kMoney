package dev.kwlew.kmoney.listeners.entities;

import dev.kwlew.kmoney.kernel.Inject;
import dev.kwlew.kmoney.listeners.ListenerComponent;
import dev.kwlew.kmoney.managers.check.CheckHandler;
import io.papermc.paper.event.player.PlayerPurchaseEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.plugin.java.JavaPlugin;

public class VillagerTradeListener implements ListenerComponent, Listener {

    private final JavaPlugin plugin;

    @Inject
    public VillagerTradeListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onVillagerTrade(PlayerPurchaseEvent event) {

        MerchantInventory inv = (MerchantInventory)
                event.getPlayer()
                        .getOpenInventory()
                        .getTopInventory();

        ItemStack first = inv.getItem(0);
        ItemStack second = inv.getItem(1);

        if (CheckHandler.isMoneyCheck(first)
                || CheckHandler.isMoneyCheck(second)) {

            event.setCancelled(true);
        }
    }
}
