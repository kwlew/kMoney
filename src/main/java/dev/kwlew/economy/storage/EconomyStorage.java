package dev.kwlew.economy.storage;

import java.util.UUID;

public interface EconomyStorage {

    double getBalance(UUID uuid);

    void setBalance(UUID uuid, double balance);

    boolean hasAccount(UUID uuid);

    boolean adminMessage(UUID uuid);

    void setAdminMessage(UUID uuid, boolean value);

    void createAccount(UUID uuid);

    void save();
}
