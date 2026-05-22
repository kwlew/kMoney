package dev.kwlew.kmoney.managers.sound;

import net.kyori.adventure.sound.Sound;

public record SoundConfig(
        boolean enabled,
        String key,
        Sound.Source source,
        float volume,
        float pitch
) {}
