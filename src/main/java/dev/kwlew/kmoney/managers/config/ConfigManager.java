package dev.kwlew.kmoney.managers.config;

import dev.kwlew.kmoney.economy.utils.MoneyParser;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;

public class ConfigManager {
    private static final BigDecimal DEFAULT_BALANCE = new BigDecimal("100.0");
    private static final int DEFAULT_TOP_UPDATE_INTERVAL_SECONDS = 30;

    private final ConfigFile config;
    private final ConfigFile messages;
    private final ConfigFile sounds;

    public ConfigManager(JavaPlugin plugin) {
        this.config = new ConfigFile(plugin, "config.yml");
        this.messages = new ConfigFile(plugin, "messages.yml");
        this.sounds = new ConfigFile(plugin, "sounds.yml");
    }

    public ConfigFile config() {
        return config;
    }

    public ConfigFile messages() {
        return messages;
    }

    public ConfigFile sounds() {
        return sounds;
    }

    public void reloadAll() {
        config.reload();
        messages.reload();
        sounds.reload();
    }

    public void saveAll() {
        config.save();
        messages.save();
        sounds.save();
    }

    public String getCurrencySymbol() {
        return config.get().getString("symbol", "$");
    }

    public boolean isJoinMessageEnabled() {
        return config.get().getBoolean("enable-join-message", true);
    }

    public boolean isUpdateWarningEnabled() {
        return config.get().getBoolean("update-warning", true);
    }

    public BigDecimal getDefaultBalance() {
        Object raw = config.get().get("default-balance");
        if (raw instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }

        if (raw instanceof String value) {
            try {
                return MoneyParser.parse(value);
            } catch (NumberFormatException ignored) {
                return DEFAULT_BALANCE;
            }
        }

        return DEFAULT_BALANCE;
    }

    public int getTopUpdateIntervalSeconds() {
        int configured = config.get().getInt("top-update-interval-seconds", DEFAULT_TOP_UPDATE_INTERVAL_SECONDS);
        return Math.max(1, configured);
    }
}