package dev.kwlew.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class MessageManager {

    private final JavaPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private Component prefixComponent;

    private FileConfiguration config;
    private File file;

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        if (file == null) {
            file = new File(plugin.getDataFolder(), "messages.yml");
        }

        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(file);

        String prefix = config.getString("prefix", "");
        prefixComponent = miniMessage.deserialize(prefix);
    }

    public void reload() {
        load();
    }

    // ======================
    // BASIC MESSAGE
    // ======================
    public Component get(String path) {
        String msg = config.getString(path);
        if (msg == null) msg = "<red>Missing message: " + path;

        return prefixComponent.append(miniMessage.deserialize(msg));
    }

    // ======================
    // ACTION BAR
    // ======================
    public void sendActionBar(Player player, String path) {
        player.sendActionBar(get(path));
    }

    public void sendActionBar(Player player, String path, TagResolver... resolvers) {
        player.sendActionBar(get(path, resolvers));
    }

    // ======================
    // WITH PLACEHOLDERS
    // ======================
    public Component get(String path, TagResolver... resolvers) {
        String msg = config.getString(path);
        if (msg == null) msg = "<red>Missing message: " + path;

        return prefixComponent.append(
                miniMessage.deserialize(msg, TagResolver.resolver(resolvers))
        );
    }

    // ======================
    // SEND METHODS
    // ======================
    public void send(Player player, String path) {
        player.sendMessage(get(path));
    }

    public void send(Player player, String path, TagResolver... resolvers) {
        player.sendMessage(get(path, resolvers));
    }

    public void send(CommandSender sender, String path) {
        sender.sendMessage(get(path));
    }

    public void send(CommandSender sender, String path, TagResolver... resolvers) {
        sender.sendMessage(get(path, resolvers));
    }

    // ======================
    // BROADCAST
    // ======================
    public void broadcast(String path) {
        org.bukkit.Bukkit.broadcast(get(path));
    }

    public void broadcast(String path, TagResolver... resolvers) {
        org.bukkit.Bukkit.broadcast(get(path, resolvers));
    }

    public TagResolver placeholder(String key, String value) {
        return Placeholder.unparsed(key, value);
    }
}
