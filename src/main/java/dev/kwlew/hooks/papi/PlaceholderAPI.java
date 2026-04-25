package dev.kwlew.hooks.papi;

import dev.kwlew.economy.api.EconomyService;
import dev.kwlew.managers.config.BuildInfo;

import dev.kwlew.managers.config.ConfigManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPI extends PlaceholderExpansion {

    private final EconomyService economy;
    private final JavaPlugin plugin;
    private final ConfigManager config;

    public PlaceholderAPI(JavaPlugin plugin, EconomyService economy, ConfigManager config) {
        this.economy = economy;
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public @NotNull String getIdentifier() {
        return BuildInfo.PLUGIN_NAME;
    }

    @Override
    public @NotNull String getAuthor() {
        return BuildInfo.AUTHOR;
    }

    @Override
    public @NotNull String getVersion() {
        return BuildInfo.VERSION;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        if (params.equalsIgnoreCase("balance")) {
            double balance = economy.getBalance(player.getUniqueId());
            return String.valueOf(balance);
        }

        if (params.equalsIgnoreCase("balance_formatted")) {
            double balance = economy.getBalance(player.getUniqueId());
            String symbol = config.getCurrencySymbol();
            return dev.kwlew.economy.utils.Formatter.format(balance, symbol);
        }

        return null;
    }
}
