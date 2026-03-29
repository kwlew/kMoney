package me.kwlew;
import me.kwlew.commands.MoneyCommand;
import me.kwlew.listeners.JoinListener;
import me.kwlew.commands.PayCommand;

import me.kwlew.listeners.QuitListener;
import me.kwlew.managers.MoneyManager;
import me.kwlew.managers.MessageManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.io.File;

public class kMoney extends JavaPlugin {

    private File playersFolder;
    private MoneyManager moneyManager;
    private MessageManager messageManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        long start = System.currentTimeMillis();

        playersFolder = new File(getDataFolder(), "players");
        if (!playersFolder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            playersFolder.mkdirs();
        }

        messageManager = new MessageManager(this);
        moneyManager = new MoneyManager(this);

        MoneyCommand moneyCommand = new MoneyCommand(this);
        PayCommand payCommand = new PayCommand(this);

        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new QuitListener(this), this);

        Objects.requireNonNull(getCommand("money")).setExecutor(moneyCommand);
        Objects.requireNonNull(getCommand("money")).setTabCompleter(moneyCommand);

        Objects.requireNonNull(getCommand("pay")).setExecutor(payCommand);
        Objects.requireNonNull(getCommand("pay")).setTabCompleter(payCommand);

        getMoneyManager().loadAll();

        long time = System.currentTimeMillis() - start;
        getLogger().info("\u001B[96mkMoney enabled!\u001B[0m");
        getLogger().info("\u001B[32mTook \u001B[32m" + time + "ms!\u001B[37m");
    }

    public File getPlayersFolder() {
        return playersFolder;
    }

    @Override
    public void onDisable() {

        getLogger().info("kMoney disabled!");
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public MoneyManager getMoneyManager() {
        return moneyManager;
    }

    public void setMessageManager(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    public void sayReload() {
        getLogger().info("\u001B[96mkMoney reloaded!\u001B[0m");
    }

    public void sayReloading() {
        getLogger().info("\u001B[96mkMoney reloading...\u001B[0m");
        getLogger().info("\u001B[96mSaving player data...\u001B[0m");
    }

}