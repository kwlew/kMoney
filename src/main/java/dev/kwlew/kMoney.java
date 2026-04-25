package dev.kwlew;

import dev.kwlew.kernel.Bootstrap;
import org.bukkit.plugin.java.JavaPlugin;

public final class kMoney extends JavaPlugin {

    private long start;
    private Bootstrap bootstrap;

    @Override
    public void onEnable() {
        start =  System.currentTimeMillis();
        saveDefaultConfig();

        bootstrap = new Bootstrap(this);
        bootstrap.init();

        logStartupTime();
    }

    @Override
    public void onDisable() {
        getLogger().info("\u001B[36mDisabling kMoney...\u001B[0m");

        if (bootstrap != null) {
            bootstrap.shutdown();
        }
    }

    private void logStartupTime() {
        long time = System.currentTimeMillis() - start;

        getLogger().info("\u001B[36mkMoney enabled! \u001B[90m(Took \u001B[32m"
                + time + "ms\u001B[90m)\u001B[0m");
    }

    public Bootstrap bootstrap() {
        return bootstrap;
    }
}