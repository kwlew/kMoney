package me.kwlew.config;

import me.kwlew.kMoney;

public class ConfigManager {
    private final ConfigFile config;
    private final ConfigFile messages;

    public ConfigManager(kMoney plugin) {
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
        return config.get().getString("currency-symbol");
    }

    public double getDefaultBalance() {
        return config.get().getDouble("player-default-money");
    }
}
