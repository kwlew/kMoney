package dev.kwlew.economy;

import dev.kwlew.economy.api.EconomyService;
import dev.kwlew.economy.storage.EconomyStorage;
import dev.kwlew.managers.config.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EconomyManager implements EconomyService {

    private final EconomyStorage storage;
    private final ConfigManager config;

    private final Map<UUID, Double> cache = new ConcurrentHashMap<>();
    private final Set<UUID> dirty = ConcurrentHashMap.newKeySet();

    private final JavaPlugin plugin;

    public EconomyManager(EconomyStorage storage, ConfigManager config, JavaPlugin plugin) {
        this.storage = storage;
        this.config = config;
        this.plugin = plugin;

        startAutosave();
    }

    @Override
    public double getBalance(UUID uuid) {
        return cache.computeIfAbsent(uuid, storage::getBalance);
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
    public boolean adminMessage(UUID uuid) {
        return storage.adminMessage(uuid);
    }

    @Override
    public void setAdminMessage(UUID uuid, boolean value) {
        storage.setAdminMessage(uuid, value);
    }

    @Override
    public boolean hasAccount(UUID uuid) {
        return cache.containsKey(uuid) || storage.hasAccount(uuid);
    }

    @Override
    public void createAccount(UUID uuid) {
        if (!hasAccount(uuid)) {
            double defaultMoney = config.getDefaultBalance();
            storage.setBalance(uuid, defaultMoney);
            storage.setAdminMessage(uuid, true);
            cache.put(uuid, defaultMoney);
            dirty.remove(uuid);
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
