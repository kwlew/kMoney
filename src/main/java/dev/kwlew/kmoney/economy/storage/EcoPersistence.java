package dev.kwlew.kmoney.economy.storage;

import dev.kwlew.kmoney.economy.api.EconomyTopEntry;
import dev.kwlew.kmoney.managers.exceptions.PlayerSaveException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
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
            return readBalance(config);
        }
    }

    /**
     * @param limit Amount of entries we get.
     * @return Returns the top entries
     */
    @Override
    public List<EconomyTopEntry> getTopEntries(int limit) {
        if (limit <= 0) {
            return List.of();
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            return List.of();
        }

        return Arrays.stream(files)
                .map(file -> {
                    String name = file.getName();
                    String uuidPart = name.substring(0, name.length() - 4); // remove .yml
                    try {
                        UUID uuid = UUID.fromString(uuidPart);
                        BigDecimal balance = getExistingBalance(uuid).orElse(BigDecimal.ZERO);
                        return new EconomyTopEntry(uuid, balance);
                    } catch (IllegalArgumentException ignored) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(EconomyTopEntry::balance).reversed())
                .limit(limit)
                .toList();
    }

    @Override
    public Optional<BigDecimal> getExistingBalance(UUID uuid) {
        synchronized (getLock(uuid)) {
            FileConfiguration cached = cache.get(uuid);
            if (cached != null) {
                return Optional.of(readBalance(cached));
            }

            File file = getFile(uuid);
            if (!file.exists()) {
                return Optional.empty();
            }

            FileConfiguration config = getCachedConfig(uuid);
            return Optional.of(readBalance(config));
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

    private BigDecimal readBalance(FileConfiguration config) {
        Object raw = config.get("balance");
        return switch (raw) {
            case Number number -> BigDecimal.valueOf(number.doubleValue());
            case String value -> {
                try {
                    yield new BigDecimal(value);
                } catch (NumberFormatException ignored) {
                    yield BigDecimal.ZERO;
                }
            }
            case null, default -> BigDecimal.ZERO;
        };
    }

}