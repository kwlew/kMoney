package me.kwlew.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.kwlew.api.EconomyService;
import me.kwlew.kMoney;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class MoneyExpansion extends PlaceholderExpansion {

    private final kMoney plugin;
    private final EconomyService economy;

    public MoneyExpansion(kMoney plugin) {
        this.plugin = plugin;
        this.economy = plugin.getEconomy();
    }


    @Override
    public @NotNull String getIdentifier() {
        return "kmoney";
    }

    @Override
    public @NotNull String getAuthor() {
        return "kwlew";
    }

    @Override
    public @NotNull String getVersion() {
        return "0.1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NonNull String params) {

        if (player == null) return "";

        if (params.equalsIgnoreCase("balance")) {
            double balance = economy.getBalance(player.getUniqueId());
            return String.valueOf(balance);
        }

        if (params.equalsIgnoreCase("balance_formatted")) {
            double balance = economy.getBalance(player.getUniqueId());
            String symbol = plugin.getConfigManager().getCurrencySymbol();
            return me.kwlew.utils.MoneyFormatter.format(balance, symbol);
        }

        return null;
    }
}
