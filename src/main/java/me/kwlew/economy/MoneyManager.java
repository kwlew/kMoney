package me.kwlew.economy;

import me.kwlew.api.EconomyService;
import me.kwlew.config.ConfigManager;
import me.kwlew.economy.storage.EconomyStorage;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class MoneyManager implements EconomyService {

    private final EconomyStorage storage;
    private final ConfigManager config;

    private final Map<UUID, Double> cache = new ConcurrentHashMap<>();
    private final Set<UUID> dirty = ConcurrentHashMap.newKeySet();

    private final JavaPlugin plugin;

    public MoneyManager(EconomyStorage storage, ConfigManager config, JavaPlugin plugin) {
        this.storage = storage;
        this.config = config;
        this.plugin = plugin;

        startAutosave();
    }

    @Override
    public double getBalance(UUID uuid) {
        if (cache.containsKey(uuid)) {
            return cache.get(uuid);
        }

        cache.put(uuid, 0.0);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            double balance = storage.getBalance(uuid);
            cache.put(uuid, balance);
        });

        return 0.0;
    }

    @Override
    public void setBalance(UUID uuid, double amount) {
        cache.put(uuid, amount);
        dirty.add(uuid);
    }

    @Override
    public void addBalance(UUID uuid, double amount) {
        setBalance(uuid, getBalance(uuid) + amount);
    }

    @Override
    public void removeBalance(UUID uuid, double amount) {
        setBalance(uuid, Math.max(0, getBalance(uuid) - amount));
    }

    @Override
    public boolean hasAccount(UUID uuid) {
        return storage.hasAccount(uuid);
    }

    @Override
    public void createAccount(UUID uuid) {
        if (!hasAccount(uuid)) {
            double defaultMoney = config.getDefaultBalance();
            cache.put(uuid, defaultMoney);
            setBalance(uuid, defaultMoney);
        }
    }

    private void saveDirty() {
        for (UUID uuid : new HashSet<>(dirty)) {
            Double balance = cache.get(uuid);
            if (balance == null) continue;

            storage.setBalance(uuid, balance);
            dirty.remove(uuid);
        }
    }

    private void startAutosave() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::saveDirty, 20L * 30, 20L * 30);
    }

    public void saveAll() {
        for (Map.Entry<UUID, Double> entry : cache.entrySet()) {
            storage.setBalance(entry.getKey(), entry.getValue());
        }
    }

}