package dev.kwlew.hooks.vault;

import dev.kwlew.economy.api.EconomyService;
import dev.kwlew.economy.utils.Formatter;
import dev.kwlew.managers.config.ConfigManager;
import net.milkbowl.vault2.economy.AccountPermission;
import net.milkbowl.vault2.economy.Economy;
import net.milkbowl.vault2.economy.EconomyResponse;
import net.milkbowl.vault2.economy.EconomyResponse.ResponseType;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class VaultUnlockedEconomy implements Economy {

    private static final String DEFAULT_CURRENCY_NAME = "Money";

    private final EconomyService economy;
    private final ConfigManager config;

    public VaultUnlockedEconomy(EconomyService economy, ConfigManager config) {
        this.economy = economy;
        this.config = config;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "kMoney";
    }

    @Override
    public boolean hasSharedAccountSupport() {
        return false;
    }

    @Override
    public boolean hasMultiCurrencySupport() {
        return false;
    }

    @Override
    public int fractionalDigits(@NotNull String pluginName) {
        return 2;
    }

    @Override
    public @NotNull String format(@NotNull BigDecimal amount) {
        return Formatter.format(amount, config.getCurrencySymbol());
    }

    @Override
    public @NotNull String format(@NotNull String pluginName, @NotNull BigDecimal amount) {
        return Formatter.format(amount, config.getCurrencySymbol());
    }

    @Override
    public @NotNull String format(@NotNull BigDecimal amount, @NotNull String currency) {
        return Formatter.format(amount, config.getCurrencySymbol());
    }

    @Override
    public @NotNull String format(@NotNull String pluginName, @NotNull BigDecimal amount, @NotNull String currency) {
        return Formatter.format(amount, config.getCurrencySymbol());
    }

    @Override
    public boolean hasCurrency(@NotNull String currency) {
        return currencyNameSingular().equalsIgnoreCase(currency);
    }

    @Override
    public @NotNull String getDefaultCurrency(@NotNull String pluginName) {
        return currencyNameSingular();
    }

    @Override
    public @NotNull String defaultCurrencyNamePlural(@NotNull String pluginName) {
        return pluralize(currencyNameSingular());
    }

    @Override
    public @NotNull String defaultCurrencyNameSingular(@NotNull String pluginName) {
        return currencyNameSingular();
    }

    @Override
    public @NotNull Collection<String> currencies() {
        return Collections.singletonList(currencyNameSingular());
    }

    @Override
    public boolean createAccount(@NotNull UUID accountID, @NotNull String name) {
        ensureAccount(accountID);
        return true;
    }

    @Override
    public boolean createAccount(@NotNull UUID accountID, @NotNull String name, boolean player) {
        return createAccount(accountID, name);
    }

    @Override
    public boolean createAccount(@NotNull UUID accountID, @NotNull String name, @NotNull String worldName) {
        return createAccount(accountID, name);
    }

    @Override
    public boolean createAccount(@NotNull UUID accountID, @NotNull String name, @NotNull String worldName, boolean player) {
        return createAccount(accountID, name);
    }

    @Override
    public @NotNull Map<UUID, String> getUUIDNameMap() {
        return Collections.emptyMap();
    }

    @Override
    public Optional<String> getAccountName(@NotNull UUID accountID) {
        return Optional.ofNullable(Bukkit.getOfflinePlayer(accountID).getName());
    }

    @Override
    public boolean hasAccount(@NotNull UUID accountID) {
        return economy.hasAccount(accountID);
    }

    @Override
    public boolean hasAccount(@NotNull UUID accountID, @NotNull String worldName) {
        return hasAccount(accountID);
    }

    @Override
    public boolean renameAccount(@NotNull UUID accountID, @NotNull String name) {
        return false;
    }

    @Override
    public boolean renameAccount(@NotNull String plugin, @NotNull UUID accountID, @NotNull String name) {
        return false;
    }

    @Override
    public boolean deleteAccount(@NotNull String plugin, @NotNull UUID accountID) {
        return false;
    }

    @Override
    public boolean accountSupportsCurrency(@NotNull String plugin, @NotNull UUID accountID, @NotNull String currency) {
        return hasAccount(accountID) && hasCurrency(currency);
    }

    @Override
    public boolean accountSupportsCurrency(@NotNull String plugin, @NotNull UUID accountID, @NotNull String currency,
                                           @NotNull String world) {
        return accountSupportsCurrency(plugin, accountID, currency);
    }

    @Override
    public @NotNull BigDecimal getBalance(@NotNull String pluginName, @NotNull UUID accountID) {
        return economy.getBalance(accountID);
    }

    @Override
    public @NotNull BigDecimal getBalance(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String world) {
        return getBalance(pluginName, accountID);
    }

    @Override
    public @NotNull BigDecimal getBalance(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String world,
                                          @NotNull String currency) {
        return getBalance(pluginName, accountID);
    }

    @Override
    public boolean has(@NotNull String pluginName, @NotNull UUID accountID, @NotNull BigDecimal amount) {
        return economy.getBalance(accountID).compareTo(amount) >= 0;
    }

    @Override
    public boolean has(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String worldName,
                       @NotNull BigDecimal amount) {
        return has(pluginName, accountID, amount);
    }

    @Override
    public boolean has(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String worldName,
                       @NotNull String currency, @NotNull BigDecimal amount) {
        return has(pluginName, accountID, amount);
    }

    @Override
    public @NotNull EconomyResponse withdraw(@NotNull String pluginName, @NotNull UUID accountID,
                                             @NotNull BigDecimal amount) {
        return withdraw(accountID, amount);
    }

    @Override
    public @NotNull EconomyResponse withdraw(@NotNull String pluginName, @NotNull UUID accountID,
                                             @NotNull String worldName, @NotNull BigDecimal amount) {
        return withdraw(accountID, amount);
    }

    @Override
    public @NotNull EconomyResponse withdraw(@NotNull String pluginName, @NotNull UUID accountID,
                                             @NotNull String worldName, @NotNull String currency,
                                             @NotNull BigDecimal amount) {
        return withdraw(accountID, amount);
    }

    @Override
    public @NotNull EconomyResponse deposit(@NotNull String pluginName, @NotNull UUID accountID,
                                            @NotNull BigDecimal amount) {
        return deposit(accountID, amount);
    }

    @Override
    public @NotNull EconomyResponse deposit(@NotNull String pluginName, @NotNull UUID accountID,
                                            @NotNull String worldName, @NotNull BigDecimal amount) {
        return deposit(accountID, amount);
    }

    @Override
    public @NotNull EconomyResponse deposit(@NotNull String pluginName, @NotNull UUID accountID,
                                            @NotNull String worldName, @NotNull String currency,
                                            @NotNull BigDecimal amount) {
        return deposit(accountID, amount);
    }

    @Override
    public boolean createSharedAccount(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String name,
                                       @NotNull UUID owner) {
        return false;
    }

    @Override
    public boolean isAccountOwner(@NotNull String pluginName, @NotNull UUID accountID, @NotNull UUID uuid) {
        return accountID.equals(uuid);
    }

    @Override
    public boolean setOwner(@NotNull String pluginName, @NotNull UUID accountID, @NotNull UUID uuid) {
        return false;
    }

    @Override
    public boolean isAccountMember(@NotNull String pluginName, @NotNull UUID accountID, @NotNull UUID uuid) {
        return accountID.equals(uuid);
    }

    @Override
    public boolean addAccountMember(@NotNull String pluginName, @NotNull UUID accountID, @NotNull UUID uuid) {
        return false;
    }

    @Override
    public boolean addAccountMember(@NotNull String pluginName, @NotNull UUID accountID, @NotNull UUID uuid,
                                    @NotNull AccountPermission... initialPermissions) {
        return false;
    }

    @Override
    public boolean removeAccountMember(@NotNull String pluginName, @NotNull UUID accountID, @NotNull UUID uuid) {
        return false;
    }

    @Override
    public boolean hasAccountPermission(@NotNull String pluginName, @NotNull UUID accountID, @NotNull UUID uuid,
                                        @NotNull AccountPermission permission) {
        return false;
    }

    @Override
    public boolean updateAccountPermission(@NotNull String pluginName, @NotNull UUID accountID, @NotNull UUID uuid,
                                           @NotNull AccountPermission permission, boolean value) {
        return false;
    }

    private EconomyResponse withdraw(UUID accountID, BigDecimal amount) {
        if (amount.signum() < 0) {
            return new EconomyResponse(BigDecimal.ZERO, economy.getBalance(accountID),
                    ResponseType.FAILURE, "Invalid amount.");
        }

        ensureAccount(accountID);
        BigDecimal balance = economy.getBalance(accountID);
        if (balance.compareTo(amount) < 0) {
            return new EconomyResponse(amount, balance, ResponseType.FAILURE, "Insufficient funds.");
        }

        economy.removeBalance(accountID, amount);
        return new EconomyResponse(amount, economy.getBalance(accountID), ResponseType.SUCCESS, "");
    }

    private EconomyResponse deposit(UUID accountID, BigDecimal amount) {
        if (amount.signum() < 0) {
            return new EconomyResponse(BigDecimal.ZERO, economy.getBalance(accountID),
                    ResponseType.FAILURE, "Invalid amount.");
        }

        ensureAccount(accountID);
        economy.addBalance(accountID, amount);
        return new EconomyResponse(amount, economy.getBalance(accountID), ResponseType.SUCCESS, "");
    }

    private void ensureAccount(UUID accountID) {
        if (!economy.hasAccount(accountID)) {
            economy.createAccount(accountID);
        }
    }

    private String currencyNameSingular() {
        String symbol = config.getCurrencySymbol();
        if (symbol != null) {
            String trimmed = symbol.trim();
            if (!trimmed.isEmpty() && trimmed.chars().allMatch(Character::isLetter)) {
                return trimmed;
            }
        }

        return DEFAULT_CURRENCY_NAME;
    }

    private String pluralize(String name) {
        if (name.endsWith("s")) {
            return name;
        }

        return name + "s";
    }
}
