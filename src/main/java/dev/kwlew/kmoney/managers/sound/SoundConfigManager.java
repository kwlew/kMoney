package dev.kwlew.kmoney.managers.sound;

import dev.kwlew.kmoney.managers.config.ConfigManager;
import net.kyori.adventure.sound.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public class SoundConfigManager {

    private static final float DEFAULT_VOLUME = 1.0f;
    private static final float DEFAULT_PITCH = 1.0f;
    private static final float MIN_PITCH = 0.1f;
    private static final float MAX_PITCH = 2.0f;
    private static final String SOUND_ROOT = "sounds";

    private final JavaPlugin plugin;
    private final ConfigManager config;

    public SoundConfigManager(JavaPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public boolean getSoundsEnabled() {
        return config.sounds().get().getBoolean("enabled", true);
    }

    public SoundConfig getSoundConfig(SoundType type) {
        return getSoundConfig(type.key());
    }

    public SoundConfig getSoundConfig(String soundId) {
        FileConfiguration soundConfig = config.sounds().get();
        String basePath = SOUND_ROOT + "." + soundId;

        boolean enabled = soundConfig.getBoolean(basePath + ".enabled", true);
        String key = soundConfig.getString(basePath + ".sound", "");
        float volume = readVolume(soundConfig, basePath + ".volume");
        float pitch = readPitch(soundConfig, basePath + ".pitch");
        Sound.Source source = readSource(soundConfig, basePath + ".source");

        return new SoundConfig(enabled, key.trim(), source, volume, pitch);
    }

    private Sound.Source readSource(FileConfiguration soundConfig, String path) {
        String raw = soundConfig.getString(path, "MASTER");
        if (raw.isBlank()) {
            return Sound.Source.MASTER;
        }

        try {
            return Sound.Source.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("Invalid sound source '" + raw + "' at " + path + ". Using MASTER.");
            return Sound.Source.MASTER;
        }
    }

    private float readVolume(FileConfiguration soundConfig, String path) {
        double value = soundConfig.getDouble(path, DEFAULT_VOLUME);
        if (!Double.isFinite(value) || value < 0.0) {
            plugin.getLogger().warning("Invalid sound volume at " + path + ". Using " + DEFAULT_VOLUME + ".");
            return DEFAULT_VOLUME;
        }

        return (float) value;
    }

    private float readPitch(FileConfiguration soundConfig, String path) {
        double value = soundConfig.getDouble(path, DEFAULT_PITCH);
        if (!Double.isFinite(value)) {
            plugin.getLogger().warning("Invalid sound pitch at " + path + ". Using " + DEFAULT_PITCH + ".");
            return DEFAULT_PITCH;
        }

        if (value < MIN_PITCH || value > MAX_PITCH) {
            double clamped = Math.clamp(value, MIN_PITCH, MAX_PITCH);
            plugin.getLogger().warning("Sound pitch at " + path + " is out of range (" + value + "). Clamping to " + clamped + ".");
            return (float) clamped;
        }

        return (float) value;
    }
}
