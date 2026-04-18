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
import me.kwlew.api.EconomyService;

import java.util.UUID;

public class JoinListener implements Listener {

    private final EconomyService economy;
    private final kMoney plugin;

    public JoinListener(kMoney plugin) {
        this.economy = plugin.getEconomy();
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();
        UUID uuid = player.getUniqueId();

        if (!economy.hasAccount(uuid)) {
            economy.createAccount(uuid);
        }

        economy.getBalance(uuid);

        if (player.hasPermission("kmoney.admin")
                && !plugin.getAdminMessageDisabled().contains(uuid)) {

            Component message = serializer.deserialize(
                    "&aYou are using &bv0.3.0b &aof kMoney!\n&aCheck for updates on my"
            ).append(
                    Component.text("GitHub")
                            .color(NamedTextColor.AQUA)
                            .clickEvent(ClickEvent.openUrl("https://github.com/kwlew/kMoney"))
            );

            player.sendMessage(message);
        }
    }
}
