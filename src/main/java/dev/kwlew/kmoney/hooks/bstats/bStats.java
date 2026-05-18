package dev.kwlew.kmoney.hooks.bstats;

import dev.kwlew.kmoney.kernel.LifecycleComponent;
import dev.kwlew.kmoney.managers.check.Check;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

import static dev.kwlew.kmoney.managers.config.BuildInfo.BSTATS_ID;

public class bStats implements LifecycleComponent {

    private final JavaPlugin plugin;
    private Metrics metrics;

    public bStats(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        initMetrics();

        uniquePlayersMetric();

        createdCheckMetrics();

        plugin.getLogger().info("Connected to bStats.");
    }

    private void initMetrics() {
        this.metrics = new Metrics(plugin, BSTATS_ID);
    }

    private void createdCheckMetrics() {
        metrics.addCustomChart(new SingleLineChart("created_checks", Check::getChecksCreated));
    }

    private void uniquePlayersMetric() {
        metrics.addCustomChart(new SingleLineChart("unique_players", () -> {

            File playersFolder = new File(plugin.getDataFolder(), "players");

            if (!playersFolder.exists() || !playersFolder.isDirectory()) {
                return 0;
            }

            File[] files = playersFolder.listFiles();
            if (files == null) return 0;

            int count = 0;
            for (File f : files) {
                if (f.isFile() && f.getName().endsWith(".yml")) {
                    count++;
                }
            }

            return count;
        }));
    }
}