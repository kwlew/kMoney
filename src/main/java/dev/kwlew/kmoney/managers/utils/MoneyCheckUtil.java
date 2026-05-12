package dev.kwlew.kmoney.managers.utils;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class MoneyCheckUtil {

    private static final NamespacedKey KEY = new NamespacedKey("kmoney", "money");

    public static boolean isMoneyCheck(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER) {
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

}
