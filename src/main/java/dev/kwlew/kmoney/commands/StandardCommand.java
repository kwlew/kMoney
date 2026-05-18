package dev.kwlew.kmoney.commands;

import dev.kwlew.kmoney.economy.api.EconomyService;
import dev.kwlew.kmoney.kernel.LifecycleComponent;
import dev.kwlew.kmoney.kernel.Inject;
import dev.kwlew.kmoney.managers.check.CheckSettings;
import dev.kwlew.kmoney.managers.utils.MessageManager;
import dev.kwlew.kmoney.managers.config.ConfigManager;
import dev.kwlew.kmoney.economy.utils.Formatter;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;

/**
 * Base class for in-game commands related to the economy.
 * <p>
 * Provides common functionality for all money commands including:
 * - Dependency injection for services
 * - Money formatting with currency symbol
 * - Common helper methods
 * <p>
 * Subclasses should implement {@link LifecycleComponent} to integrate with the plugin lifecycle.
 */
public abstract class StandardCommand implements LifecycleComponent {

    protected final JavaPlugin plugin;
    protected final EconomyService economy;
    protected final MessageManager messages;
    protected final ConfigManager config;
    protected final CheckSettings checkSettings;

    /**
     * Creates a new base command with injected dependencies.
     *
     * @param plugin the plugin instance
     * @param economy the economy service
     * @param messages the message manager for localized messages
     * @param config the configuration manager
     * @param checkSettings check settings manager.
     */
    @Inject
    protected StandardCommand(JavaPlugin plugin, EconomyService economy, MessageManager messages, ConfigManager config, CheckSettings checkSettings) {
        this.plugin = plugin;
        this.economy = economy;
        this.messages = messages;
        this.config = config;
        this.checkSettings = checkSettings;
    }

    /**
     * Formats a monetary amount using the configured currency symbol.
     *
     * @param amount the amount to format
     * @return formatted string with currency symbol and suffix (e.g., "$1.5M")
     */
    protected String formatMoney(BigDecimal amount) {
        return Formatter.format(amount, config.getCurrencySymbol());
    }
}