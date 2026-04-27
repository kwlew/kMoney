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

/**
 * Manages all player-facing messages and notifications for the kMoney plugin.
 * 
 * This manager provides:
 * - MiniMessage text formatting (colors, decorations, styles)
 * - Automatic prefix prepending to messages
 * - Placeholder/tag substitution for dynamic values
 * - Convenient methods for sending messages to players
 * 
 * ARCHITECTURE:
 * - Uses Adventure API's MiniMessage for rich text formatting
 * - Loads messages from messages.yml configuration file
 * - Caches the global prefix for efficiency
 * - Supports both prefixed and raw message retrieval
 * 
 * MESSAGE METHODS:
 * - get(path) / get(path, resolvers) - Returns formatted Component with prefix
 * - getRaw(path) / getRaw(path, resolvers) - Returns Component without prefix
 * - send(...) - Sends message to player or CommandSender
 * - sendActionBar(...) - Sends message to player's action bar
 * - broadcast(...) - Broadcasts message to all players
 * 
 * FORMATTING:
 * MiniMessage uses tags for formatting:
 * - Colors: <red>, <green>, <blue>, <yellow>, <aqua>, etc.
 * - Hex colors: <#FF0000>
 * - Decorations: <bold>, <italic>, <underlined>, <strikethrough>
 * - Closing: </> or </tag>
 * 
 * PLACEHOLDER SYSTEM:
 * Messages use tags for dynamic values: <player>, <amount>, <balance>
 * Supply values using: messages.placeholder("player", "Steve")
 * 
 * Example usage:
 * {@code
 * messages.send(player, "money.balance-self",
 *     messages.placeholder("balance", "$1,234.56")
 * );
 * }
 * 
 * @see <a href="https://docs.advntr.dev/minimessage/format.html">MiniMessage Format</a>
 */
public class MessageManager {

    private final JavaPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private Component prefixComponent;

    private FileConfiguration config;
    private File file;

    /**
     * Creates a new message manager and loads messages from config.
     *
     * @param plugin the plugin instance
     */
    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    /**
     * Loads messages from the messages.yml file.
     * Creates the file from defaults if it doesn't exist.
     * Extracts and caches the prefix component.
     */
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

    /**
     * Reloads messages from disk.
     * Useful for /reload commands to update messages without restart.
     */
    public void reload() {
        load();
    }

    // ======================
    // BASIC MESSAGE
    // ======================
    
    /**
     * Gets a formatted message component with prefix.
     * Uses MiniMessage formatting and includes the global prefix.
     *
     * @param path the message path in config (e.g., "money.balance-self")
     * @return formatted Component with prefix, or error message if not found
     */
    public Component get(String path) {
        String msg = config.getString(path);
        if (msg == null) msg = "<red>Missing message: " + path;

        return prefixComponent.append(miniMessage.deserialize(msg));
    }

    // ======================
    // ACTION BAR
    // ======================
    
    /**
     * Sends a message to a player's action bar (top of screen).
     * Action bar messages have the prefix prepended.
     *
     * @param player the player to send to
     * @param path the message path in config
     */
    public void sendActionBar(Player player, String path) {
        player.sendActionBar(get(path));
    }

    /**
     * Sends a message to a player's action bar with placeholder substitution.
     *
     * @param player the player to send to
     * @param path the message path in config
     * @param resolvers tag resolvers for placeholder substitution
     */
    public void sendActionBar(Player player, String path, TagResolver... resolvers) {
        player.sendActionBar(get(path, resolvers));
    }

    // ======================
    // WITH PLACEHOLDERS
    // ======================
    
    /**
     * Gets a formatted message component with prefix and placeholder substitution.
     * Placeholders are specified using tag resolvers.
     * Example: messages.placeholder("player", "Steve")
     *
     * @param path the message path in config (e.g., "money.pay-sent")
     * @param resolvers tag resolvers for placeholder replacement
     * @return formatted Component with prefix and substituted values
     */
    public Component get(String path, TagResolver... resolvers) {
        String msg = config.getString(path);
        if (msg == null) msg = "<red>Missing message: " + path;

        return prefixComponent.append(
                miniMessage.deserialize(msg, TagResolver.resolver(resolvers))
        );
    }

    /**
     * Gets a formatted message component WITHOUT prefix.
     * Used for item lore, book text, and other contexts where prefix shouldn't appear.
     *
     * @param path the message path in config
     * @return formatted Component without prefix, or error message if not found
     */
    public Component getRaw(String path) {
        String msg = config.getString(path);
        if (msg == null) msg = "<red>Missing message: " + path;

        return miniMessage.deserialize(msg);
    }

    /**
     * Gets a formatted message component WITHOUT prefix and with placeholder substitution.
     * Used for item lore and other contexts requiring dynamic values without prefix.
     *
     * @param path the message path in config
     * @param resolvers tag resolvers for placeholder substitution
     * @return formatted Component without prefix and with substituted values
     */
    public Component getRaw(String path, TagResolver... resolvers) {
        String msg = config.getString(path);
        if (msg == null) msg = "<red>Missing message: " + path;

        return miniMessage.deserialize(msg, TagResolver.resolver(resolvers));
    }

    // ======================
    // SEND METHODS
    // ======================
    
    /**
     * Sends a message to a player.
     * Includes the global prefix automatically.
     *
     * @param player the player to receive the message
     * @param path the message path in config
     */
    public void send(Player player, String path) {
        player.sendMessage(get(path));
    }

    /**
     * Sends a message to a player with placeholder substitution.
     * Includes the global prefix automatically.
     *
     * @param player the player to receive the message
     * @param path the message path in config
     * @param resolvers tag resolvers for placeholder replacement
     */
    public void send(Player player, String path, TagResolver... resolvers) {
        player.sendMessage(get(path, resolvers));
    }

    /**
     * Sends a message to any CommandSender (player or console).
     * Includes the global prefix automatically.
     *
     * @param sender the CommandSender to receive the message
     * @param path the message path in config
     */
    public void send(CommandSender sender, String path) {
        sender.sendMessage(get(path));
    }

    /**
     * Sends a message to any CommandSender with placeholder substitution.
     * Includes the global prefix automatically.
     *
     * @param sender the CommandSender to receive the message
     * @param path the message path in config
     * @param resolvers tag resolvers for placeholder replacement
     */
    public void send(CommandSender sender, String path, TagResolver... resolvers) {
        sender.sendMessage(get(path, resolvers));
    }

    // ======================
    // BROADCAST
    // ======================
    
    /**
     * Broadcasts a message to all players on the server.
     * Includes the global prefix automatically.
     *
     * @param path the message path in config
     */
    public void broadcast(String path) {
        org.bukkit.Bukkit.broadcast(get(path));
    }

    /**
     * Broadcasts a message to all players with placeholder substitution.
     * Includes the global prefix automatically.
     *
     * @param path the message path in config
     * @param resolvers tag resolvers for placeholder replacement
     */
    public void broadcast(String path, TagResolver... resolvers) {
        org.bukkit.Bukkit.broadcast(get(path, resolvers));
    }

    // ======================
    // PLACEHOLDER HELPERS
    // ======================
    
    /**
     * Creates a placeholder tag resolver for dynamic message substitution.
     * Use with send() methods to replace tags in messages.
     * 
     * Example:
     * {@code
     * messages.send(player, "money.pay-sent",
     *     messages.placeholder("player", "Steve"),
     *     messages.placeholder("amount", "$100")
     * );
     * }
     *
     * @param key the placeholder key as it appears in the message (e.g., "player")
     * @param value the value to substitute (e.g., "Steve")
     * @return a tag resolver for this placeholder
     */
    public TagResolver placeholder(String key, String value) {
        return Placeholder.unparsed(key, value);
    }
}
