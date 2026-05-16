package dev.kwlew.kmoney.managers.check;

import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.EnumSet;
import java.util.Set;

public class CheckHandler {

    private static final NamespacedKey KEY = new NamespacedKey("kmoney", "money");

    private static final Set<InventoryType> BLACKLIST = EnumSet.of(
            InventoryType.CRAFTER,
            InventoryType.ANVIL,
            InventoryType.SMITHING,
            InventoryType.FURNACE,
            InventoryType.SMOKER,
            InventoryType.BEACON,
            InventoryType.BLAST_FURNACE,
            InventoryType.CARTOGRAPHY,
            InventoryType.ENCHANTING,
            InventoryType.MERCHANT,
            InventoryType.LOOM,
            InventoryType.STONECUTTER,
            InventoryType.GRINDSTONE
    );

    public static boolean isMoneyCheck(ItemStack item) {
        if (item == null || item.getType() != Check.getMaterial()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        String encoded = meta.getPersistentDataContainer().get(KEY, PersistentDataType.STRING);
        if (encoded != null) {
            return true;
        }

        Double legacy = meta.getPersistentDataContainer().get(KEY, PersistentDataType.DOUBLE);
        return legacy != null;
    }

    public static boolean isBlacklistedInventory(InventoryType type) {
        return BLACKLIST.contains(type);
    }

}
