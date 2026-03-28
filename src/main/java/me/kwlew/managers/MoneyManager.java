package me.kwlew.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.UUID;
import java.io.File;
import java.io.IOException;

import me.kwlew.kMoney;

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

    public void removeMoney(UUID uuid, int amount) {
        if (!playerData.containsKey(uuid)){
            loadPlayer(uuid);
        }
        int current = getMoney(uuid);
        setMoney(uuid, current-amount);
    }
}
