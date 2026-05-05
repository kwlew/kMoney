package dev.kwlew.kmoney.hooks.bstats;

import dev.kwlew.kmoney.kernel.LifecycleComponent;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class bStats implements LifecycleComponent {

    private final JavaPlugin plugin;

    public bStats(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        int pluginId = 31039;

        Metrics metrics = new Metrics(plugin, pluginId);

        plugin.getLogger().info("Connected to bStats.");
    }
}