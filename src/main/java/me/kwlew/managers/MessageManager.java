package me.kwlew.managers;

import me.kwlew.kMoney;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class MessageManager {

    private final kMoney plugin;
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();

    private FileConfiguration messagesConfig;
    private File messagesFile;

    public MessageManager(kMoney plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        if(!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reloadMessages() {
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public Component get(String path, String... replacements) {
        String msg = messagesConfig.getString("messages." + path);
        String prefix = messagesConfig.getString("prefix", "");

        if (msg == null) {
            return Component.text("Missing message: " + path,
                    net.kyori.adventure.text.format.NamedTextColor.DARK_RED);
        }

        msg = msg.replace("{prefix}", prefix);

        for (int i = 0; i < replacements.length; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }

        return serializer.deserialize(msg);
    }
}