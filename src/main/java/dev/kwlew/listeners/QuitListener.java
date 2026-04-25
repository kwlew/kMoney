package dev.kwlew.listeners;

import dev.kwlew.economy.EconomyManager;
import dev.kwlew.kernel.Inject;
import org.bukkit.entity.Player;
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
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        economy.setBalance(uuid, economy.getBalance(uuid));
    }
}
