package dev.kwlew.kmoney.economy.storage;

import dev.kwlew.kmoney.economy.api.EconomyTopEntry;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EconomyStorage {

    BigDecimal getBalance(UUID uuid);

    List<EconomyTopEntry> getTopEntries(int limit);

    Optional<BigDecimal> getExistingBalance(UUID uuid);

    void setBalance(UUID uuid, BigDecimal balance);

    boolean hasAccount(UUID uuid);

    boolean adminMessage(UUID uuid);

    void setAdminMessage(UUID uuid, boolean value);

    void createAccount(UUID uuid);

    void save();
}