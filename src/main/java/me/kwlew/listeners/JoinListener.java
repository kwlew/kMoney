package me.kwlew.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import me.kwlew.kMoney;

public class JoinListener implements Listener {
    private final kMoney plugin;

    public JoinListener(kMoney plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getMoneyManager().loadPlayer(event.getPlayer().getUniqueId());
        Player player = event.getPlayer();

        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();

        if (player.hasPermission("money.admin")) {

            Component message = serializer.deserialize(
                    "&aYou are using &bv0.0.1 &aof kMoney!\n&aCheck for updates: "
            ).append(
                    Component.text("Here")
                            .color(NamedTextColor.AQUA)
                            .clickEvent(ClickEvent.openUrl("https://github.com/kwlew/kMoney"))
            );

            player.sendMessage(message);
        }
    }
}
