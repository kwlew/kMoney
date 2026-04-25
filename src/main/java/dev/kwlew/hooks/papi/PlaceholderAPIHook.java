package dev.kwlew.hooks.papi;

import dev.kwlew.economy.api.EconomyService;
import dev.kwlew.kernel.LifecycleComponent;
import dev.kwlew.managers.config.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PlaceholderAPIHook implements LifecycleComponent {

    private final JavaPlugin plugin;
    private final EconomyService economy;
    private final ConfigManager config;

    public PlaceholderAPIHook(JavaPlugin plugin, EconomyService economy, ConfigManager config) {
        this.plugin = plugin;
        this.economy = economy;
        this.config = config;
    }

    @Override
    public void start() {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPI(plugin, economy, config).register();

            plugin.getLogger().info("\u001B[36mkMoney hooked to [PlaceholderAPI]\u001B[0m");
        }
    }
}
