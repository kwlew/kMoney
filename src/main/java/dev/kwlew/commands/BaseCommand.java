package dev.kwlew.commands;

import dev.kwlew.economy.api.EconomyService;
import dev.kwlew.kernel.LifecycleComponent;
import dev.kwlew.kernel.Inject;
import dev.kwlew.managers.MessageManager;
import dev.kwlew.managers.config.ConfigManager;
import dev.kwlew.economy.utils.Formatter;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class BaseCommand implements LifecycleComponent {

    protected final JavaPlugin plugin;
    protected final EconomyService economy;
    protected final MessageManager messages;
    protected final ConfigManager config;

    @Inject
    protected BaseCommand(JavaPlugin plugin, EconomyService economy, MessageManager messages, ConfigManager config) {
        this.plugin = plugin;
        this.economy = economy;
        this.messages = messages;
        this.config = config;
    }

    protected String formatMoney(double amount) {
        return Formatter.format(amount, config.getCurrencySymbol());
    }
}
