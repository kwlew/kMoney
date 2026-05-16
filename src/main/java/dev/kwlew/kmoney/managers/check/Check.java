package dev.kwlew.kmoney.managers.check;

import org.bukkit.Material;

public final class Check {

    private static final Material DEFAULT_MATERIAL = Material.PAPER;
    private static Material material = DEFAULT_MATERIAL;

    private Check() {}

    public static Material getMaterial() {
        return material;
    }

    public static void setMaterial(Material material) {
        Check.material = material;
    }
}
