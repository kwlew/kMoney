package dev.kwlew.kmoney.listeners.player;

import dev.kwlew.kmoney.economy.EconomyManager;
import dev.kwlew.kmoney.kernel.Inject;
import dev.kwlew.kmoney.listeners.ListenerComponent;
import dev.kwlew.kmoney.managers.config.BuildInfo;
import dev.kwlew.kmoney.managers.updater.UpdateParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class JoinListener implements ListenerComponent {

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    private final EconomyManager economy;
    private final JavaPlugin plugin;
    private final UpdateParser updateParser;

    @Inject
    public JoinListener(JavaPlugin plugin, EconomyManager economy, UpdateParser updateParser) {
        this.plugin = plugin;
        this.economy = economy;
        this.updateParser = updateParser;
    }

    @Override
    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!economy.hasAccount(player.getUniqueId())) {
            economy.createAccount(player.getUniqueId());
        }

        if (!player.hasPermission("kmoney.admin")) {
            return;
        }

        if (!economy.adminMessage(player.getUniqueId())) {
            return;
        }

        player.sendMessage(buildAdminMessage());
    }

    private Component buildAdminMessage() {
        if (updateParser.isOutdated()) {
            String latestVersion = updateParser.getLatestVersion();
            String latestText = latestVersion == null ? "unknown" : latestVersion;

            return SERIALIZER.deserialize(
                            "&aYou are using &bv" + BuildInfo.VERSION + " &aof kMoney.\n"
                                    + "&cA new version is available: &bv" + latestText + "&c. Download at "
                    )
                    .append(
                            Component.text("Modrinth")
                                    .color(NamedTextColor.DARK_GREEN)
                                    .clickEvent(ClickEvent.openUrl(BuildInfo.MODRINTH_URL))
                    );
        }

        return SERIALIZER.deserialize(
                        "&aYou are using &bv" + BuildInfo.VERSION + " &aof kMoney!\n&aSource code on "
                )
                .append(
                        Component.text("GitHub")
                                .color(NamedTextColor.AQUA)
                                .clickEvent(ClickEvent.openUrl(BuildInfo.GITHUB_URL))
                );
    }
}