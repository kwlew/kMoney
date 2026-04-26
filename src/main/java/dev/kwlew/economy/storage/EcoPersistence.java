package dev.kwlew.economy.storage;

import dev.kwlew.managers.exceptions.PlayerSaveException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class EcoPersistence implements EconomyStorage {

    private final File folder;

    private final Map<UUID, FileConfiguration> cache = new ConcurrentHashMap<>();

    private final Map<UUID, Object> locks = new ConcurrentHashMap<>();

    public EcoPersistence(JavaPlugin plugin) {
        this.folder = new File(plugin.getDataFolder(), "players");

        if (!folder.exists()) {
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
    public BigDecimal getBalance(UUID uuid) {
        synchronized (getLock(uuid)) {
            FileConfiguration config = getCachedConfig(uuid);
            Object raw = config.get("balance");
            switch (raw) {
                case null -> {
                    return BigDecimal.ZERO;
                }
                case Number number -> {
                    return BigDecimal.valueOf(number.doubleValue());
                }
                case String value -> {
                    try {
                        return new BigDecimal(value);
                    } catch (NumberFormatException ignored) {
                        return BigDecimal.ZERO;
                    }
                }
                default -> {
                }
            }

            return BigDecimal.ZERO;
        }
    }

    @Override
    public boolean adminMessage(UUID uuid) {
        synchronized (getLock(uuid)) {
            FileConfiguration config = getCachedConfig(uuid);
            return config.getBoolean("adminMessage", true);
        }
    }

    @Override
    public void setAdminMessage(UUID uuid, boolean value) {
        synchronized (getLock(uuid)) {
            File file =  getFile(uuid);
            FileConfiguration config = getCachedConfig(uuid);

            config.set("adminMessage", value);

            try {
                config.save(file);
            } catch (IOException e) {
                throw new PlayerSaveException(uuid);
            }
        }
    }

    @Override
    public void setBalance(UUID uuid, BigDecimal amount) {
        synchronized (getLock(uuid)) {
            File file = getFile(uuid);
            FileConfiguration config = getCachedConfig(uuid);

            config.set("balance", amount.toPlainString());

            try {
                config.save(file);
            } catch (IOException e) {
                throw new PlayerSaveException(uuid);
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
                setBalance(uuid, BigDecimal.ZERO);
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
