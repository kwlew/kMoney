package dev.kwlew.economy.api;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service for managing player economy accounts and balances.
 * <p>
 * This interface defines core operations for the economy system including balance queries,
 * modifications, and account management. All balance operations are safe from negative values
 * as the system enforces non-negative constraints.
 */
public interface EconomyService {

    /**
     * Gets the current balance for a player.
     *
     * @param uuid the player's unique ID
     * @return the player's current balance as BigDecimal, never negative
     */
    BigDecimal getBalance(UUID uuid);

    /**
     * Sets a player's balance to a specific amount.
     * If the amount is negative, it is automatically clamped to zero.
     *
     * @param uuid the player's unique ID
     * @param amount the new balance amount
     * @throws IllegalArgumentException if amount is null
     */
    void setBalance(UUID uuid, BigDecimal amount);

    /**
     * Adds an amount to a player's balance.
     * The resulting balance cannot go below zero.
     *
     * @param uuid the player's unique ID
     * @param amount the amount to add (should be positive)
     * @throws IllegalArgumentException if amount is null
     */
    void addBalance(UUID uuid, BigDecimal amount);

    /**
     * Removes an amount from a player's balance.
     * The resulting balance cannot go below zero.
     *
     * @param uuid the player's unique ID
     * @param amount the amount to remove (should be positive)
     * @throws IllegalArgumentException if amount is null
     */
    void removeBalance(UUID uuid, BigDecimal amount);

    /**
     * Checks if a player has admin messages enabled.
     * Admin messages notify admins about certain economy operations.
     *
     * @param uuid the player's unique ID
     * @return true if admin messages are enabled, false otherwise
     */
    boolean adminMessage(UUID uuid);

    /**
     * Sets whether a player receives admin messages.
     *
     * @param uuid the player's unique ID
     * @param value true to enable admin messages, false to disable
     */
    void setAdminMessage(UUID uuid, boolean value);

    /**
     * Checks if a player has an economy account.
     *
     * @param uuid the player's unique ID
     * @return true if the account exists, false otherwise
     */
    boolean hasAccount(UUID uuid);

    /**
     * Creates a new economy account for a player with the default starting balance.
     * If the account already exists, this method does nothing.
     *
     * @param uuid the player's unique ID
     */
    void createAccount(UUID uuid);
}
