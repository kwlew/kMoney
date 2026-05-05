package dev.kwlew.kmoney.managers.config;

import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;

public class ConfigManager {
    private final ConfigFile config;
    private final ConfigFile messages;

    public ConfigManager(JavaPlugin plugin) {
        this.config = new ConfigFile(plugin, "config.yml");
        this.messages = new ConfigFile(plugin, "messages.yml");
    }

    public ConfigFile config() {
        return config;
    }

    public ConfigFile messages() {
        return messages;
    }

    public void reloadAll() {
        config.reload();
        messages.reload();
    }

    public void saveAll() {
        config.save();
        messages.save();
    }

    public String getCurrencySymbol() {
        return config.get().getString("symbol", "$");
    }

    public BigDecimal getDefaultBalance() {
        Object raw = config.get().get("default-balance");
        if (raw instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }

        if (raw instanceof String value) {
            try {
                return new BigDecimal(value);
            } catch (NumberFormatException ignored) {
                return new BigDecimal("100.0");
            }
        }

        return new BigDecimal("100.0");
    }
}