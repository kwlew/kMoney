package dev.kwlew.kmoney.managers.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ConfigFile {

    private final JavaPlugin plugin;
    private final String name;

    private File file;
    private FileConfiguration config;

    public ConfigFile(JavaPlugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
        load();
    }

    private void load() {
        file = new File(plugin.getDataFolder(), name);
        ensureFileExists();

        config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration get() {
        return config;
    }

    public void reload() {
        ensureFileExists();
        config = YamlConfiguration.loadConfiguration(file);
    }

    private void ensureFileExists() {
        if (file.exists()) {
            return;
        }

        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();
        plugin.saveResource(name, false);
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}