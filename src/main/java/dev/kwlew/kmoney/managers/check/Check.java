package dev.kwlew.kmoney.managers.check;

import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Material;

public final class Check {

    private static final Material DEFAULT_MATERIAL = Material.PAPER;
    private static Material material = DEFAULT_MATERIAL;

    private static final AtomicInteger checksCreated = new AtomicInteger(0);

    private Check() {}

    public static Material getMaterial () {
        return material;
    }

    public static void setMaterial(Material material) {
        Check.material = material;
    }

    public static void incrementChecksCreated() {
        checksCreated.incrementAndGet();
    }

    public static int getChecksCreated() {
        return checksCreated.get();
    }
}
