package dev.kwlew.economy.utils;

import java.math.BigDecimal;

/**
 * Utility class for validating monetary input and values.
 * Provides methods to validate money amounts, player names, and other monetary constraints.
 */
public class MoneyValidator {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal MAX_SAFE_VALUE = new BigDecimal("9.99999999E+99");

    /**
     * Validates and parses a money amount from string input.
     *
     * @param input the string input representing a money amount (e.g., "100", "1k", "5.5m")
     * @return the parsed BigDecimal amount
     * @throws IllegalArgumentException if the input is null, empty, invalid format,
     *         negative, or exceeds maximum safe value
     */
    public static BigDecimal validateMoneyAmount(String input) throws IllegalArgumentException {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Money amount cannot be null or empty");
        }

        BigDecimal amount;
        try {
            amount = MoneyParser.parse(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid money amount format: '" + input + "'. " +
                    "Use numbers (e.g., 100, 1.5) or suffixes (k, m, b, t, etc.)", e);
        }

        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Money amount cannot be negative: " + amount);
        }

        if (amount.compareTo(MAX_SAFE_VALUE) > 0) {
            throw new IllegalArgumentException("Money amount exceeds maximum safe value: " + amount);
        }

        return amount;
    }

    /**
     * Validates that an amount is positive (greater than zero).
     *
     * @param amount the BigDecimal amount to validate
     * @throws IllegalArgumentException if the amount is zero or negative
     */
    public static void validatePositiveAmount(BigDecimal amount) throws IllegalArgumentException {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive, got: " + amount);
        }
    }

    /**
     * Validates that a balance is non-negative.
     *
     * @param balance the BigDecimal balance to validate
     * @throws IllegalArgumentException if the balance is negative
     */
    public static void validateBalance(BigDecimal balance) throws IllegalArgumentException {
        if (balance == null) {
            throw new IllegalArgumentException("Balance cannot be null");
        }
        if (balance.signum() < 0) {
            throw new IllegalArgumentException("Balance cannot be negative: " + balance);
        }
    }

    /**
     * Validates a player name for basic constraints.
     *
     * @param playerName the name to validate
     * @throws IllegalArgumentException if the name is null, empty, or invalid
     */
    public static void validatePlayerName(String playerName) throws IllegalArgumentException {
        if (playerName == null || playerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be null or empty");
        }

        String trimmed = playerName.trim();
        if (trimmed.length() < 3 || trimmed.length() > 16) {
            throw new IllegalArgumentException("Invalid player name: must be 3-16 characters, got: " + playerName);
        }

        if (!trimmed.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Invalid player name: can only contain letters, numbers, and underscores. Got: " + playerName);
        }
    }

    /**
     * Validates the number of notes for check withdrawal.
     *
     * @param notes the number of notes
     * @throws IllegalArgumentException if notes is outside valid range (1-64)
     */
    public static void validateNotesCount(int notes) throws IllegalArgumentException {
        if (notes < 1 || notes > 64) {
            throw new IllegalArgumentException("Notes count must be between 1 and 64, got: " + notes);
        }
    }

    /**
     * Validates that a player has sufficient balance for a transaction.
     *
     * @param balance the player's current balance
     * @param amount the amount to transfer/withdraw
     * @throws IllegalArgumentException if balance is insufficient
     */
    public static void validateSufficientBalance(BigDecimal balance, BigDecimal amount)
            throws IllegalArgumentException {
        if (balance == null || amount == null) {
            throw new IllegalArgumentException("Balance and amount cannot be null");
        }

        if (balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance. Required: " + amount +
                    ", Available: " + balance);
        }
    }
}
