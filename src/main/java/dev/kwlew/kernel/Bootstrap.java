package dev.kwlew.kernel;

import dev.kwlew.economy.EconomyManager;
import dev.kwlew.economy.api.EconomyService;
import dev.kwlew.economy.storage.EcoPersistence;
import dev.kwlew.economy.storage.EconomyStorage;
import dev.kwlew.commands.MoneyCommand;
import dev.kwlew.hooks.papi.PlaceholderAPIHook;
import dev.kwlew.listeners.CheckClaimListener;
import dev.kwlew.listeners.CraftListener;
import dev.kwlew.listeners.JoinListener;
import dev.kwlew.listeners.QuitListener;
import dev.kwlew.managers.MessageManager;
import dev.kwlew.managers.config.ConfigManager;
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
    }

    private void lifecycle(Consumer<LifecycleComponent> action) {
        for (Object obj : new ArrayList<>(registry.getAll())) {
            if (obj instanceof LifecycleComponent component) {
                action.accept(component);
            }
        }
    }
}
