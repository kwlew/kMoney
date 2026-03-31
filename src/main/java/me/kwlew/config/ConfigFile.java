package me.kwlew.config;

import me.kwlew.kMoney;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigFile {

    private final kMoney plugin;
    private final String name;

    private File file;
    private FileConfiguration config;

    public ConfigFile(kMoney plugin, String name) {
        this.plugin = plugin;
        this.name = name;
        load();
    }

    private void load() {
        file = new File(plugin.getDataFolder(), name);

        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
            plugin.saveResource(name, false);
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration get() {
        return config;
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
