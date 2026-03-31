package me.kwlew.economy;

import me.kwlew.api.EconomyService;
import me.kwlew.config.ConfigManager;
import me.kwlew.economy.storage.EconomyStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MoneyManager implements EconomyService {

    private final EconomyStorage storage;
    private final ConfigManager config;

    private final Map<UUID, Double> cache = new HashMap<>();

    public MoneyManager(EconomyStorage storage, ConfigManager config) {
        this.storage = storage;
        this.config = config;
    }

    @Override
    public double getBalance(UUID uuid) {
        if (cache.containsKey(uuid)) {
            return cache.get(uuid);
        }

        double balance = storage.getBalance(uuid);
        cache.put(uuid, balance);

        return balance;
    }

    @Override
    public void setBalance(UUID uuid, double amount) {
        cache.put(uuid, amount);

        storage.setBalance(uuid, amount);
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

    public void saveAll() {
        for (Map.Entry<UUID, Double> entry : cache.entrySet()) {
            storage.setBalance(entry.getKey(), entry.getValue());
        }
    }
}