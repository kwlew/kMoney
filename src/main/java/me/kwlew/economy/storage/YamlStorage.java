package me.kwlew.economy.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class YamlStorage implements EconomyStorage {

    private final File folder;
    private final JavaPlugin plugin;

    public YamlStorage(JavaPlugin plugin) {

        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "players");

        if (!folder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            folder.mkdirs();
        }
    }

    private File getFile(UUID uuid) {
        return new File(folder, uuid.toString() + ".yml");
    }

    private FileConfiguration getConfig(UUID uuid) {
        return YamlConfiguration.loadConfiguration(getFile(uuid));
    }

    @Override
    public double getBalance(UUID uuid) {
        FileConfiguration config = getConfig(uuid);
        return config.getDouble("balance", 0.0);
    }

    @Override
    public void setBalance(UUID uuid, double amount) {
        File file = getFile(uuid);
        FileConfiguration config = getConfig(uuid);

        config.set("balance", amount);

        try {
            config.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasAccount(UUID uuid) {
        return getFile(uuid).exists();
    }

    @Override
    public void createAccount(UUID uuid) {
        if (!hasAccount(uuid)) {
            setBalance(uuid, 0.0);
        }
    }

    @Override
    public void save() {
        // Not needed (we save per operation)
    }
}