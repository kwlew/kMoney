package me.kwlew.economy.storage;

import java.util.UUID;

public interface EconomyStorage {

    double getBalance(UUID uuid);

    void setBalance(UUID uuid, double amount);

    boolean hasAccount(UUID uuid);

    void createAccount(UUID uuid);

    void save();
}