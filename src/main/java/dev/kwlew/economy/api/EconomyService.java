package dev.kwlew.economy.api;

import java.math.BigDecimal;
import java.util.UUID;

public interface EconomyService {

    BigDecimal getBalance(UUID uuid);

    void setBalance(UUID uuid, BigDecimal amount);

    void addBalance(UUID uuid, BigDecimal amount);

    void removeBalance(UUID uuid, BigDecimal amount);

    boolean adminMessage(UUID uuid);

    void setAdminMessage(UUID uuid, boolean value);

    boolean hasAccount(UUID uuid);

    void createAccount(UUID uuid);
}
