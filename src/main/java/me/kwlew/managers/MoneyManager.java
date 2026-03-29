package me.kwlew.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.UUID;
import java.io.File;
import java.io.IOException;

import me.kwlew.kMoney;
import org.bukkit.entity.Player;

public class MoneyManager {

    private final kMoney plugin;

    private final HashMap<UUID, FileConfiguration> playerData = new HashMap<>();
    private final HashMap<UUID, File> playerFiles = new HashMap<>();

    public MoneyManager(kMoney plugin) {
        this.plugin = plugin;
    }

    public void loadPlayer(UUID uuid){
        File file = new File(plugin.getPlayersFolder(), uuid.toString() + ".yml");

        if (!file.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();

                FileConfiguration config = YamlConfiguration.loadConfiguration(file);

                int defaultMoney = plugin.getConfig().getInt("player-default-money");
                config.set("money", defaultMoney);

                config.save(file);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        playerFiles.put(uuid, file);
        playerData.put(uuid, config);
    }

    public void savePlayer(UUID uuid){
        if (!playerData.containsKey(uuid)) return;

        try {
            playerData.get(uuid).save(playerFiles.get(uuid));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            savePlayer(uuid);
        }
    }

    public void loadAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            loadPlayer(uuid);
        }
    }

    public void unloadPlayer(UUID uuid) {
        savePlayer(uuid);
        playerData.remove(uuid);
        playerFiles.remove(uuid);
    }

    public int getMoney(UUID uuid) {
        if (!playerData.containsKey(uuid)) {
            loadPlayer(uuid);
        }
        return playerData.get(uuid).getInt("money");
    }

    public void setMoney(UUID uuid, int amount) {
        if (!playerData.containsKey(uuid)){
            loadPlayer(uuid);
        }
        playerData.get(uuid).set("money", amount);
    }

    public void addMoney(UUID uuid, int amount) {
        if (!playerData.containsKey(uuid)) {
            loadPlayer(uuid);
        }
        int current = getMoney(uuid);
        setMoney(uuid, current+amount);
    }

    public boolean isNumberInvalid(int amount) {
        return amount < 0;
    }

    public boolean canPay(int amount, UUID payer) {
        return getMoney(payer)-amount >= 0;
    }

    public void removeMoney(UUID uuid, int amount) {
        if (!playerData.containsKey(uuid)){
            loadPlayer(uuid);
        }
        int current = getMoney(uuid);
        if (current-amount < 0) {
            amount -= amount-current;
        }
        setMoney(uuid, current-amount);
    }
}
