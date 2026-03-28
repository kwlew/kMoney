package me.kwlew.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import me.kwlew.kMoney;

public class QuitListener implements Listener {

    private final kMoney plugin;

    public QuitListener(kMoney plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerJoinEvent event) {
        plugin.getMoneyManager().unloadPlayer(event.getPlayer().getUniqueId());
    }
}
