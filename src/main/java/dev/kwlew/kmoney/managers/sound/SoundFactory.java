package dev.kwlew.kmoney.managers.sound;

import dev.kwlew.kmoney.kernel.LifecycleComponent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumMap;
import java.util.logging.Level;

public class SoundFactory implements LifecycleComponent {

    private final JavaPlugin plugin;
    private final SoundConfigManager soundConfig;

    private final EnumMap<SoundType, Sound> sounds = new EnumMap<>(SoundType.class);
    private boolean soundsEnabled = true;

    public SoundFactory(JavaPlugin plugin, SoundConfigManager soundConfig) {
        this.plugin = plugin;
        this.soundConfig = soundConfig;
    }

    @Override
    public void start() {
        plugin.getLogger().log(Level.INFO, "Starting SoundFactory");
        reload();
    }

    private void loadSounds() {
        sounds.clear();
        if (!soundsEnabled) {
            return;
        }

        for (SoundType type : SoundType.values()) {
            SoundConfig config = soundConfig.getSoundConfig(type);
            Sound sound = buildSound(config, type);
            if (sound != null) {
                sounds.put(type, sound);
            }
        }
    }

    public void reload() {
        soundsEnabled = soundConfig.getSoundsEnabled();
        loadSounds();
    }

    public void play(Player player, SoundType type) {
        if (!soundsEnabled || player == null) {
            return;
        }

        Sound sound = sounds.get(type);
        if (sound != null) {
            player.playSound(sound);
        }
    }

    private Sound buildSound(SoundConfig config, SoundType type) {
        if (config == null || !config.enabled()) {
            return null;
        }

        String key = config.key();
        if (key == null || key.isBlank()) {
            return null;
        }

        try {
            return Sound.sound(Key.key(key.trim()), config.source(), config.volume(), config.pitch());
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("Invalid sound key for " + type.key() + ": " + key);
            return null;
        }
    }
}
