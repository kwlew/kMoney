package dev.kwlew.kmoney.listeners;

import dev.kwlew.kmoney.economy.EconomyManager;
import dev.kwlew.kmoney.kernel.Inject;
import dev.kwlew.kmoney.managers.config.BuildInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class JoinListener implements ListenerComponent {

    private final EconomyManager economy;
    private final JavaPlugin plugin;

    @Inject
    public JoinListener(JavaPlugin plugin, EconomyManager economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    @Override
    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();

        if (!economy.hasAccount(player.getUniqueId())) {
            economy.createAccount(player.getUniqueId());
        }

        if (economy.adminMessage(player.getUniqueId()) && player.hasPermission("kmoney.admin")) {
            Component message = serializer.deserialize(
                    "&aYou are using &bv" + BuildInfo.VERSION + " &aof kMoney!\n&aCheck for updates on my "
            ).append(
                    Component.text("GitHub")
                            .color(NamedTextColor.AQUA)
                            .clickEvent(ClickEvent.openUrl(BuildInfo.GITHUB_URL))
            );

            player.sendMessage(message);
        }
    }
}