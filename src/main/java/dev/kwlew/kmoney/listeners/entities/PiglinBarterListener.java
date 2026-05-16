package dev.kwlew.kmoney.listeners.entities;

import dev.kwlew.kmoney.listeners.ListenerComponent;
import dev.kwlew.kmoney.managers.check.CheckHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class PiglinBarterListener implements ListenerComponent, Listener {

    private final JavaPlugin plugin;

    public PiglinBarterListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBarter(PiglinBarterEvent event) {
        ItemStack item = event.getInput();

        if (CheckHandler.isMoneyCheck(item)) {
            event.setCancelled(true);
        }
    }
}
