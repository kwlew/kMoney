package me.kwlew;

import me.kwlew.api.EconomyService;
import me.kwlew.commands.MoneyCommand;
import me.kwlew.commands.PayStandaloneCommand;
import me.kwlew.commands.subcommands.PayCommand;
import me.kwlew.config.ConfigManager;
import me.kwlew.config.MessageManager;
import me.kwlew.economy.MoneyManager;
import me.kwlew.economy.storage.YamlStorage;
import me.kwlew.listeners.CraftListener;
import me.kwlew.listeners.JoinListener;
import me.kwlew.listeners.MoneyRedeemListener;
import me.kwlew.listeners.QuitListener;
import me.kwlew.placeholder.MoneyExpansion;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class kMoney extends JavaPlugin {

    private ConfigManager configManager;
    private EconomyService economy;

    private final Set<UUID> adminMessageDisabled = new HashSet<>();

    public Set<UUID> getAdminMessageDisabled() {
        return adminMessageDisabled;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        long start = System.currentTimeMillis();
        configManager = new ConfigManager(this);

        YamlStorage storage = new YamlStorage(this);
        economy = new MoneyManager(storage, configManager, this);

        getServer().getServicesManager().register(EconomyService.class,
                economy,
                this,
                org.bukkit.plugin.ServicePriority.Normal
        );

        MessageManager messageManager = new MessageManager(this);
        MoneyCommand moneyCommand = new MoneyCommand(economy, messageManager, configManager, this);
        PayCommand payCommand = new PayCommand(economy, messageManager, configManager);

        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new QuitListener(this), this);
        getServer().getPluginManager().registerEvents(new MoneyRedeemListener(economy, this, messageManager), this);
        getServer().getPluginManager().registerEvents(new CraftListener(this), this);

        Objects.requireNonNull(getCommand("pay")).setExecutor(new PayStandaloneCommand(payCommand));
        Objects.requireNonNull(getCommand("money")).setTabCompleter(moneyCommand);
        Objects.requireNonNull(getCommand("money")).setExecutor(moneyCommand);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MoneyExpansion(this).register();
            getLogger().info("\u001B[36mkMoney hooked to [PlaceholderAPI]\u001B[0m");
        }

        long time = System.currentTimeMillis() - start;
        startTime(time);
    }

    @Override
    public void onDisable() {
        getLogger().info("\u001B[36mDisabling kMoney...\u001B[0m");

        if (economy instanceof MoneyManager manager) {
            manager.saveAll();
        }
        saveConfig();
    }

    private void startTime(long time) {
        getLogger().info("\u001B[36mkMoney enabled! \u001B[90m(Took \u001B[32m"
                + time + "ms\u001B[90m)\u001B[0m");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public EconomyService getEconomy() {
        return economy;
    }
}