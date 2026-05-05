package dev.kwlew.kmoney.hooks.vault;

import dev.kwlew.kmoney.economy.api.EconomyService;
import dev.kwlew.kmoney.economy.utils.Formatter;
import dev.kwlew.kmoney.managers.config.ConfigManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class VaultEconomy implements Economy {

    private final EconomyService economy;
    private final ConfigManager config;

    public VaultEconomy(EconomyService economy, ConfigManager config) {
        this.economy = economy;
        this.config = config;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "kMoney";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double amount) {
        return Formatter.format(BigDecimal.valueOf(amount), config.getCurrencySymbol());
    }

    @Override
    public String currencyNamePlural() {
        return VaultCurrency.pluralize(currencyNameSingular());
    }

    @Override
    public String currencyNameSingular() {
        return VaultCurrency.resolveCurrencyName(config);
    }

    @Override
    public boolean hasAccount(String accountId) {
        return economy.hasAccount(resolveUuid(accountId));
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return economy.hasAccount(player.getUniqueId());
    }

    @Override
    public boolean hasAccount(String accountId, String worldName) {
        return hasAccount(accountId);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public double getBalance(String accountId) {
        return economy.getBalance(resolveUuid(accountId)).doubleValue();
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return economy.getBalance(player.getUniqueId()).doubleValue();
    }

    @Override
    public double getBalance(String accountId, String worldName) {
        return getBalance(accountId);
    }

    @Override
    public double getBalance(OfflinePlayer player, String worldName) {
        return getBalance(player);
    }

    @Override
    public boolean has(String accountId, double amount) {
        return economy.getBalance(resolveUuid(accountId)).compareTo(BigDecimal.valueOf(amount)) >= 0;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return economy.getBalance(player.getUniqueId()).compareTo(BigDecimal.valueOf(amount)) >= 0;
    }

    @Override
    public boolean has(String accountId, String worldName, double amount) {
        return has(accountId, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String accountId, double amount) {
        return withdraw(resolveUuid(accountId), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        return withdraw(player.getUniqueId(), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String accountId, String worldName, double amount) {
        return withdrawPlayer(accountId, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String accountId, double amount) {
        return deposit(resolveUuid(accountId), amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        return deposit(player.getUniqueId(), amount);
    }

    @Override
    public EconomyResponse depositPlayer(String accountId, String worldName, double amount) {
        return depositPlayer(accountId, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return notImplemented("Banks are not supported.");
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return notImplemented("Banks are not supported.");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return notImplemented("Banks are not supported.");
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return notImplemented("Banks are not supported.");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return notImplemented("Banks are not supported.");
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return notImplemented("Banks are not supported.");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return notImplemented("Banks are not supported.");
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return notImplemented("Banks are not supported.");
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return notImplemented("Banks are not supported.");
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return notImplemented("Banks are not supported.");
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return notImplemented("Banks are not supported.");
    }

    @Override
    public List<String> getBanks() {
        return Collections.emptyList();
    }

    @Override
    public boolean createPlayerAccount(String accountId) {
        UUID uuid = resolveUuid(accountId);
        if (economy.hasAccount(uuid)) {
            return false;
        }

        economy.createAccount(uuid);
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        if (economy.hasAccount(uuid)) {
            return false;
        }

        economy.createAccount(uuid);
        return true;
    }

    @Override
    public boolean createPlayerAccount(String accountId, String worldName) {
        return createPlayerAccount(accountId);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }

    private EconomyResponse withdraw(UUID uuid, double amount) {
        if (isInvalidAmount(amount)) {
            return new EconomyResponse(0.0, economy.getBalance(uuid).doubleValue(),
                    EconomyResponse.ResponseType.FAILURE, "Invalid amount.");
        }

        ensureAccount(uuid);
        BigDecimal amountValue = BigDecimal.valueOf(amount);
        BigDecimal balance = economy.getBalance(uuid);

        if (balance.compareTo(amountValue) < 0) {
            return new EconomyResponse(amount, balance.doubleValue(), EconomyResponse.ResponseType.FAILURE, "Insufficient funds.");
        }

        economy.removeBalance(uuid, amountValue);
        return new EconomyResponse(amount, economy.getBalance(uuid).doubleValue(), EconomyResponse.ResponseType.SUCCESS, "");
    }

    private EconomyResponse deposit(UUID uuid, double amount) {
        if (isInvalidAmount(amount)) {
            return new EconomyResponse(0.0, economy.getBalance(uuid).doubleValue(),
                    EconomyResponse.ResponseType.FAILURE, "Invalid amount.");
        }

        ensureAccount(uuid);
        BigDecimal amountValue = BigDecimal.valueOf(amount);
        economy.addBalance(uuid, amountValue);
        return new EconomyResponse(amount, economy.getBalance(uuid).doubleValue(), EconomyResponse.ResponseType.SUCCESS, "");
    }

    private boolean isInvalidAmount(double amount) {
        return Double.isNaN(amount) || Double.isInfinite(amount) || amount < 0;
    }

    private void ensureAccount(UUID uuid) {
        if (!economy.hasAccount(uuid)) {
            economy.createAccount(uuid);
        }
    }

    private EconomyResponse notImplemented(String message) {
        return new EconomyResponse(0.0, 0.0,
                EconomyResponse.ResponseType.NOT_IMPLEMENTED, message);
    }

    private UUID resolveUuid(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            return UUID.nameUUIDFromBytes("unknown".getBytes(StandardCharsets.UTF_8));
        }

        try {
            return UUID.fromString(accountId);
        } catch (IllegalArgumentException ignored) {
            return Bukkit.getOfflinePlayer(accountId).getUniqueId();
        }
    }

}