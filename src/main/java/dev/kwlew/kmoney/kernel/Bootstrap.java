package dev.kwlew.kmoney.kernel;

import dev.kwlew.kmoney.economy.EconomyManager;
import dev.kwlew.kmoney.economy.api.EconomyService;
import dev.kwlew.kmoney.economy.storage.EcoPersistence;
import dev.kwlew.kmoney.economy.storage.EconomyStorage;
import dev.kwlew.kmoney.commands.MoneyCommand;
import dev.kwlew.kmoney.hooks.bstats.bStats;
import dev.kwlew.kmoney.hooks.papi.PlaceholderAPIHook;
import dev.kwlew.kmoney.hooks.vault.VaultHook;
import dev.kwlew.kmoney.listeners.CheckClaimListener;
import dev.kwlew.kmoney.listeners.CraftListener;
import dev.kwlew.kmoney.listeners.JoinListener;
import dev.kwlew.kmoney.listeners.QuitListener;
import dev.kwlew.kmoney.managers.MessageManager;
import dev.kwlew.kmoney.managers.config.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.function.Consumer;

public class Bootstrap {

    private final Registry registry = new Registry();

    public Bootstrap(JavaPlugin plugin) {
        registry.register(JavaPlugin.class, plugin);
        registry.register(Registry.class, registry);

        initManagers();

        initEconomy();

        initCommands();

        initHooks();

        initListeners();
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
        registry.resolve(JoinListener.class);
        registry.resolve(QuitListener.class);
        registry.resolve(CheckClaimListener.class);
        registry.resolve(CraftListener.class);
    }

    private void initEconomy() {
        registry.bind(EconomyStorage.class, EcoPersistence.class);

        registry.bind(EconomyService.class, EconomyManager.class);
    }

    private void initCommands() {
        registry.resolve(MoneyCommand.class);
    }

    private void initHooks() {
        registry.resolve(PlaceholderAPIHook.class);
        registry.resolve(VaultHook.class);
        registry.resolve(bStats.class);
    }

    private void lifecycle(Consumer<LifecycleComponent> action) {
        for (Object obj : new ArrayList<>(registry.getAll())) {
            if (obj instanceof LifecycleComponent component) {
                action.accept(component);
            }
        }
    }
}