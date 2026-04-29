package dev.kwlew.hooks.vault;

import dev.kwlew.economy.api.EconomyService;
import dev.kwlew.kernel.LifecycleComponent;
import dev.kwlew.managers.config.ConfigManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

public class VaultHook implements LifecycleComponent {

    private final JavaPlugin plugin;
    private final EconomyService economy;
    private final ConfigManager config;

    private VaultEconomy vaultEconomy;
    private VaultUnlockedEconomy vaultUnlockedEconomy;

    public VaultHook(JavaPlugin plugin, EconomyService economy, ConfigManager config) {
        this.plugin = plugin;
        this.economy = economy;
        this.config = config;
    }

    @Override
    public void start() {
        Plugin vaultPlugin = plugin.getServer().getPluginManager().getPlugin("Vault");
        if (vaultPlugin == null || !vaultPlugin.isEnabled()) {
            return;
        }

        ServicesManager services = plugin.getServer().getServicesManager();

        boolean hookedVault = false;
        boolean hookedVaultUnlocked = false;

        if (classExists("net.milkbowl.vault2.economy.Economy")) {
            vaultUnlockedEconomy = new VaultUnlockedEconomy(economy, config);
            services.register(net.milkbowl.vault2.economy.Economy.class, vaultUnlockedEconomy, plugin, ServicePriority.Highest);
            plugin.getLogger().info("\u001B[36mkMoney hooked to [VaultUnlocked]\u001B[0m");
            hookedVaultUnlocked = true;
        }

        if (classExists("net.milkbowl.vault.economy.Economy")) {
            vaultEconomy = new VaultEconomy(economy, config);
            services.register(net.milkbowl.vault.economy.Economy.class, vaultEconomy, plugin, ServicePriority.Highest);
            plugin.getLogger().info("\u001B[36mkMoney hooked to [Vault]\u001B[0m");
            hookedVault = true;
        }

        if (!hookedVault && !hookedVaultUnlocked) {
            plugin.getLogger().warning("Couldn't hook to Vault! Is it installed?");
        }
    }

    @Override
    public void shutdown() {
        ServicesManager services = plugin.getServer().getServicesManager();
        if (vaultUnlockedEconomy != null) {
            services.unregister(net.milkbowl.vault2.economy.Economy.class, vaultUnlockedEconomy);
            vaultUnlockedEconomy = null;
        }

        if (vaultEconomy != null) {
            services.unregister(net.milkbowl.vault.economy.Economy.class, vaultEconomy);
            vaultEconomy = null;
        }
    }

    private boolean classExists(String className) {
        try {
            Class.forName(className, false, plugin.getClass().getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
