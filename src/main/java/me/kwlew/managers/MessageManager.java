package me.kwlew.managers;

import me.kwlew.kMoney;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class MessageManager {

    private final kMoney plugin;
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();

    public MessageManager(kMoney plugin) {
        this.plugin = plugin;
    }

    public Component get(String path, String... replacements) {
        String msg = plugin.getConfig().getString("messages." + path);
        String prefix = plugin.getConfig().getString("prefix", "");

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