package dev.kwlew.managers.config;

import dev.kwlew.kMoney;
import org.bukkit.plugin.java.JavaPlugin;

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

    public double getDefaultBalance() {
        return 100.0;
    }
}
