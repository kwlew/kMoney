package me.kwlew.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import me.kwlew.kMoney;

public class QuitListener implements Listener {

    private final kMoney plugin;

    public QuitListener(kMoney plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getEconomy().setBalance(
                event.getPlayer().getUniqueId(),
                plugin.getEconomy().getBalance(event.getPlayer().getUniqueId())
        );
    }
}