package dev.kwlew.kmoney.managers.check;

import org.bukkit.Material;

public final class Check {

    private static final Material default_material = Material.PAPER;
    private static Material material = default_material;

    private Check() {}

    public static Material getMaterial() {
        return material;
    }

    public static void setMaterial(Material material) {
        Check.material = material;
    }
}
