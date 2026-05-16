package dev.kwlew.kmoney.kernel;

import dev.kwlew.kmoney.commands.MoneyCommand;
import dev.kwlew.kmoney.economy.EconomyManager;
import dev.kwlew.kmoney.economy.api.EconomyService;
import dev.kwlew.kmoney.economy.storage.EcoPersistence;
import dev.kwlew.kmoney.economy.storage.EconomyStorage;
import dev.kwlew.kmoney.hooks.bstats.bStats;
import dev.kwlew.kmoney.hooks.papi.PlaceholderAPIHook;
import dev.kwlew.kmoney.hooks.vault.VaultHook;
import dev.kwlew.kmoney.listeners.player.CheckClaimListener;
import dev.kwlew.kmoney.listeners.player.JoinListener;
import dev.kwlew.kmoney.listeners.player.QuitListener;
import dev.kwlew.kmoney.listeners.craft.CraftListener;
import dev.kwlew.kmoney.listeners.entities.PiglinBarterListener;
import dev.kwlew.kmoney.listeners.entities.VillagerTradeListener;
import dev.kwlew.kmoney.listeners.gui.HopperListener;
import dev.kwlew.kmoney.listeners.gui.InventoryClickListener;
import dev.kwlew.kmoney.managers.check.CheckHandler;
import dev.kwlew.kmoney.managers.check.CheckSettings;
import dev.kwlew.kmoney.managers.config.ConfigManager;
import dev.kwlew.kmoney.managers.utils.MessageManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.function.Consumer;

public class Bootstrap {

    private final Registry registry = new Registry();

    public Bootstrap(JavaPlugin plugin) {
        registry.register(JavaPlugin.class, plugin);
        registry.register(Registry.class, registry);

        initMain();
    }

    public void init() {
        lifecycle(LifecycleComponent::init);
        lifecycle(LifecycleComponent::start);

    }

    public void shutdown() {
        lifecycle(LifecycleComponent::shutdown);
    }

    private void initManagers() {
        registry.resolve(MessageManager.class);
        registry.resolve(ConfigManager.class);
    }

    private void initListeners() {
        playerListeners();

        preventionListeners();
    }

    private void playerListeners() {
        registry.resolve(JoinListener.class);
        registry.resolve(QuitListener.class);

        registry.resolve(CheckClaimListener.class);
    }

    private void preventionListeners() {
        registry.resolve(CraftListener.class);
        registry.resolve(HopperListener.class);
        registry.resolve(InventoryClickListener.class);
        registry.resolve(VillagerTradeListener.class);
        registry.resolve(PiglinBarterListener.class);
    }

    private void initEconomy() {
        registry.bind(EconomyStorage.class, EcoPersistence.class);

        registry.bind(EconomyService.class, EconomyManager.class);
    }

    private void initUtils() {
        registry.resolve(CheckHandler.class);
    }

    private void initCommands() {
        registry.resolve(MoneyCommand.class);
    }

    private void initHooks() {
        registry.resolve(PlaceholderAPIHook.class);
        registry.resolve(VaultHook.class);
        registry.resolve(bStats.class);
    }

    private void initChecks() {
        registry.resolve(CheckSettings.class);
    }

    private void initMain() {
        initManagers();
        initEconomy();
        initCommands();
        initHooks();
        initUtils();
        initListeners();
        initChecks();
    }

    private void lifecycle(Consumer<LifecycleComponent> action) {
        for (Object obj : new ArrayList<>(registry.getAll())) {
            if (obj instanceof LifecycleComponent component) {
                action.accept(component);
            }
        }
    }
}