package dev.kwlew.kmoney.economy;

import dev.kwlew.kmoney.economy.api.EconomyService;
import dev.kwlew.kmoney.economy.api.EconomyTopEntry;
import dev.kwlew.kmoney.economy.storage.EconomyStorage;
import dev.kwlew.kmoney.kernel.LifecycleComponent;
import dev.kwlew.kmoney.managers.config.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player economy accounts with in-memory caching and persistent storage.
 * <p>
 * This implementation provides:
 * - In-memory balance cache for fast access
 * - Automatic dirty tracking for efficient persistence
 * - Asynchronous autosave every 30 seconds
 * - Thread-safe balance operations
 * - Prevents negative balances
 * <p>
 * The economy manager uses a write-back cache pattern where changes are cached in memory
 * and periodically persisted to storage in the background.
 */
public class EconomyManager implements EconomyService, LifecycleComponent {

    private final EconomyStorage storage;
    private final ConfigManager config;

    private final Map<UUID, BigDecimal> cache = new ConcurrentHashMap<>();
    private final Set<UUID> dirty = ConcurrentHashMap.newKeySet();
    private volatile List<EconomyTopEntry> topCache = List.of();

    private final JavaPlugin plugin;
    private BukkitTask autosaveTask;
    private BukkitTask topUpdateTask;

    /**
     * Creates a new economy manager.
     *
     * @param storage the persistence layer for saving/loading balances
     * @param config the configuration manager for defaults
     * @param plugin the plugin instance for scheduling tasks
     */
    public EconomyManager(EconomyStorage storage, ConfigManager config, JavaPlugin plugin) {
        this.storage = storage;
        this.config = config;
        this.plugin = plugin;

        startAutosave();
        startTopUpdater();
    }

    @Override
    public BigDecimal getBalance(UUID uuid) {
        BigDecimal cached = cache.get(uuid);
        if (cached != null) {
            return cached;
        }

        BigDecimal loaded = storage.getExistingBalance(uuid).orElse(null);
        if (loaded == null) {
            return BigDecimal.ZERO;
        }

        cache.put(uuid, loaded);
        return loaded;
    }

    @Override
    public void setBalance(UUID uuid, BigDecimal amount) {
        cache.put(uuid, amount.max(BigDecimal.ZERO));
        dirty.add(uuid);
    }

    @Override
    public void addBalance(UUID uuid, BigDecimal amount) {
        setBalance(uuid, getBalance(uuid).add(amount));
    }

    @Override
    public void removeBalance(UUID uuid, BigDecimal amount) {
        setBalance(uuid, getBalance(uuid).subtract(amount).max(BigDecimal.ZERO));
    }

    @Override
    public boolean adminMessage(UUID uuid) {
        return storage.adminMessage(uuid);
    }

    @Override
    public void setAdminMessage(UUID uuid, boolean value) {
        storage.setAdminMessage(uuid, value);
    }

    @Override
    public boolean hasAccount(UUID uuid) {
        return dirty.contains(uuid) || storage.hasAccount(uuid);
    }

    @Override
    public void createAccount(UUID uuid) {
        if (!hasAccount(uuid)) {
            BigDecimal defaultMoney = config.getDefaultBalance();
            storage.setBalance(uuid, defaultMoney);
            storage.setAdminMessage(uuid, true);
            cache.put(uuid, defaultMoney);
            dirty.remove(uuid);
        }
    }

    @Override
    public List<EconomyTopEntry> getTopEntries(int limit) {
        if (limit <= 0) {
            return List.of();
        }

        List<EconomyTopEntry> cached = topCache;
        if (cached.isEmpty()) {
            return List.of();
        }

        if (cached.size() <= limit) {
            return cached;
        }

        return List.copyOf(cached.subList(0, limit));
    }

    /**
     * Saves all modified accounts to persistent storage.
     * Runs synchronously and should only be called when necessary.
     */
    private synchronized void saveDirty() {
        for (UUID uuid : new HashSet<>(dirty)) {
            flushAccount(uuid);
        }
    }

    /**
     * Starts the automatic save task that runs every 30 seconds asynchronously.
     */
    private void startAutosave() {
        autosaveTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                plugin,
                this::saveDirty,
                20L * 30,
                20L * 30
        );
    }

    private void startTopUpdater() {
        rescheduleTopUpdater();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::refreshTopCache);
    }

    public synchronized void rescheduleTopUpdater() {
        if (topUpdateTask != null) {
            topUpdateTask.cancel();
        }

        int intervalSeconds = config.getTopUpdateIntervalSeconds();
        long intervalTicks = 20L * intervalSeconds;

        topUpdateTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                plugin,
                this::refreshTopCache,
                intervalTicks,
                intervalTicks
        );
    }

    private void refreshTopCache() {
        saveDirty();
        topCache = List.copyOf(storage.getTopEntries(Integer.MAX_VALUE));
    }

    @Override
    public synchronized void shutdown() {
        if (autosaveTask != null) {
            autosaveTask.cancel();
        }

        if (topUpdateTask != null) {
            topUpdateTask.cancel();
        }

        saveAll();
    }

    /**
     * Immediately saves all cached balances to persistent storage.
     * Used during plugin shutdown to ensure no data loss.
     */
    public synchronized void saveAll() {
        for (Map.Entry<UUID, BigDecimal> entry : cache.entrySet()) {
            storage.setBalance(entry.getKey(), entry.getValue());
        }

        dirty.clear();
    }

    public synchronized void flushAccount(UUID uuid) {
        if (!dirty.contains(uuid)) {
            return;
        }

        BigDecimal balance = cache.get(uuid);
        if (balance == null) {
            dirty.remove(uuid);
            return;
        }

        storage.setBalance(uuid, balance);
        dirty.remove(uuid);
    }
}