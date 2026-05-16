package dev.kwlew.kmoney.managers.settings;

import dev.kwlew.kmoney.kernel.LifecycleComponent;
import dev.kwlew.kmoney.managers.config.ConfigManager;
import org.bukkit.Material;

public class CheckSettings implements LifecycleComponent {

    private final ConfigManager config;

    private Material material;

    public CheckSettings(ConfigManager config) {
        this.config = config;
    }

    @Override
    public void init() {

    }

    public void reload() {
        String name = config.config().get()
                .getString("check-material", "PAPER");
    }
}
