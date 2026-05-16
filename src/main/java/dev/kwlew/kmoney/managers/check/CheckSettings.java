package dev.kwlew.kmoney.managers.check;

import dev.kwlew.kmoney.kernel.LifecycleComponent;
import dev.kwlew.kmoney.managers.config.ConfigManager;
import org.bukkit.Material;

public class CheckSettings implements LifecycleComponent {

    private final ConfigManager config;

    public CheckSettings(ConfigManager config) {
        this.config = config;
    }

    @Override
    public void init() {
        reload();
    }

    public void reload() {
        String name = config.config().get()
                .getString("check-material", "PAPER");

        Material parsed = Material.matchMaterial(name);

        if (parsed == null) {
            parsed = Material.PAPER;
        }

        Check.setMaterial(parsed);
    }
}
