package dev.kwlew.economy.api;

import java.util.UUID;

public interface EconomyService {

    double getBalance(UUID uuid);

    void setBalance(UUID uuid, double amount);

    void addBalance(UUID uuid, double amount);

    void removeBalance(UUID uuid, double amount);

    boolean adminMessage(UUID uuid);

    void setAdminMessage(UUID uuid, boolean value);

    boolean hasAccount(UUID uuid);

    void createAccount(UUID uuid);
}
