package dev.kwlew.kmoney.hooks.papi;

import dev.kwlew.kmoney.economy.api.EconomyService;
import dev.kwlew.kmoney.economy.utils.Formatter;
import dev.kwlew.kmoney.managers.config.BuildInfo;

import dev.kwlew.kmoney.managers.config.ConfigManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

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
            BigDecimal balance = economy.getBalance(player.getUniqueId());
            return balance.stripTrailingZeros().toPlainString();
        }

        if (params.equalsIgnoreCase("balance_formatted")) {
            BigDecimal balance = economy.getBalance(player.getUniqueId());
            String symbol = config.getCurrencySymbol();
            return Formatter.format(balance, symbol);
        }

        return null;
    }
}