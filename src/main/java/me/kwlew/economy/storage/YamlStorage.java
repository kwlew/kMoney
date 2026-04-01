package me.kwlew.economy.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class YamlStorage implements EconomyStorage {

    private final File folder;

    private final Map<UUID, FileConfiguration> cache = new ConcurrentHashMap<>();

    private final Map<UUID, Object> locks = new ConcurrentHashMap<>();


    public YamlStorage(JavaPlugin plugin) {
        this.folder = new File(plugin.getDataFolder(), "players");

        if (!folder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            folder.mkdirs();
        }
    }

    private File getFile(UUID uuid) {
        return new File(folder, uuid.toString() + ".yml");
    }

    private Object getLock(UUID uuid) {
        return locks.computeIfAbsent(uuid, u -> new Object());
    }

    private FileConfiguration getCachedConfig(UUID uuid) {
        return cache.computeIfAbsent(uuid, u ->
                YamlConfiguration.loadConfiguration(getFile(u))
        );
    }

    @Override
    public double getBalance(UUID uuid) {
        synchronized (getLock(uuid)) {
            FileConfiguration config = getCachedConfig(uuid);
            return config.getDouble("balance", 0.0);
        }
    }

    @Override
    public void setBalance(UUID uuid, double amount) {
        synchronized (getLock(uuid)) {
            File file = getFile(uuid);
            FileConfiguration config = getCachedConfig(uuid);

            config.set("balance", amount);

            try {
                config.save(file);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save balance for " + uuid);
            }
        }
    }

    @Override
    public boolean hasAccount(UUID uuid) {
        return getFile(uuid).exists();
    }

    @Override
    public void createAccount(UUID uuid) {
        synchronized (getLock(uuid)) {
            if (!hasAccount(uuid)) {
                setBalance(uuid, 0.0);
            }
        }
    }

    @Override
    public void save() {
        for (UUID uuid : cache.keySet()) {
            synchronized (getLock(uuid)) {
                try {
                    cache.get(uuid).save(getFile(uuid));
                } catch (IOException e) {
                    throw new RuntimeException("Failed to save " + uuid, e);
                }
            }
        }
    }

}