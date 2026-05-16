package dev.kwlew.kmoney.listeners.player;

import dev.kwlew.kmoney.economy.EconomyManager;
import dev.kwlew.kmoney.kernel.Inject;
import dev.kwlew.kmoney.listeners.ListenerComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class QuitListener implements ListenerComponent {

    private final JavaPlugin plugin;
    private final EconomyManager economy;

    @Inject
    public QuitListener(JavaPlugin plugin, EconomyManager economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    @Override
    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        economy.flushAccount(uuid);
    }
}