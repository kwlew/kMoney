package me.kwlew.config;

import me.kwlew.kMoney;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MessageManager {

    private FileConfiguration config;
    private final LegacyComponentSerializer serializer;
    private final kMoney plugin;
    private final File file;

    public MessageManager(kMoney plugin) {
        this.plugin = plugin;

        this.file = new File(plugin.getDataFolder(), "messages.yml");

        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        this.config = YamlConfiguration.loadConfiguration(file);
        this.serializer = LegacyComponentSerializer.legacyAmpersand();
    }

    public Component get(String path) {
        String message = config.getString(path, "&cMissing message: " + path);
        return serializer.deserialize(message);
    }

    public Component get(String path, String... replacements) {
        String message = config.getString(path, "&cMissing message: " + path);

        for (int i = 0; i < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }

        return serializer.deserialize(message);
    }

    public Component getWithPrefix(String path, String... replacements) {
        String prefix = config.getString("prefix", "");
        String message = config.getString(path, "&cMissing message: " + path);

        for (int i = 0; i < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }

        return serializer.deserialize(prefix + message);
    }

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(file);
    }
}