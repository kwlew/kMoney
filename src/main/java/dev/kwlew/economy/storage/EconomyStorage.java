package dev.kwlew.economy.storage;

import java.math.BigDecimal;
import java.util.UUID;

public interface EconomyStorage {

    BigDecimal getBalance(UUID uuid);

    void setBalance(UUID uuid, BigDecimal balance);

    boolean hasAccount(UUID uuid);

    boolean adminMessage(UUID uuid);

    void setAdminMessage(UUID uuid, boolean value);

    void createAccount(UUID uuid);

    void save();
}
