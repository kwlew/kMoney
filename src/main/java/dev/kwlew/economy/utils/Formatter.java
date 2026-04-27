package dev.kwlew.economy.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Formats monetary amounts with currency symbol and magnitude suffixes for display.
 * <p>
 * Large numbers are automatically scaled with suffixes:
 * - K: thousands (1e3)
 * - M: millions (1e6)
 * - B: billions (1e9)
 * - T: trillions (1e12)
 * - Q: quadrillions (1e15)
 * - QQ: quintillions (1e18)
 * - S: sextillions (1e21)
 * - SS: septillions (1e24)
 * - OC: octillions (1e27)
 * - NO: nonillions (1e30)
 * - DC: decillions (1e33)
 * - UDC: undecillions (1e36)
 * <p>
 * Examples: "$100", "$1.5K", "$2.3M", "-$500"
 */
public class Formatter {
    private static final String[] SUFFIXES = {
            "",
            "K",   // 1e3
            "M",   // 1e6
            "B",   // 1e9
            "T",   // 1e12
            "Q",   // 1e15
            "QQ",  // 1e18
            "S",   // 1e21
            "SS",  // 1e24
            "OC",  // 1e27
            "NO",  // 1e30
            "DC",  // 1e33
            "UDC"  // 1e36
    };
    private static final BigDecimal THOUSAND = new BigDecimal("1000");
    private static final int DISPLAY_SCALE = 2;

    /**
     * Formats a monetary amount with currency symbol and suffix.
     *
     * @param amount the amount to format
     * @param symbol the currency symbol (e.g., "$", "€", "£")
     * @return formatted string with symbol, value, and suffix (e.g., "$1.5M")
     */
    public static String format(BigDecimal amount, String symbol) {
        boolean negative = amount.signum() < 0;
        BigDecimal value = amount.abs();

        int index = 0;

        while (value.compareTo(THOUSAND) >= 0 && index < SUFFIXES.length - 1) {
            value = value.divide(THOUSAND, 12, RoundingMode.HALF_UP);
            index++;
        }

        BigDecimal rounded = value.setScale(DISPLAY_SCALE, RoundingMode.HALF_UP);
        if (rounded.compareTo(THOUSAND) >= 0 && index < SUFFIXES.length - 1) {
            value = rounded.divide(THOUSAND, 12, RoundingMode.HALF_UP);
            rounded = value.setScale(DISPLAY_SCALE, RoundingMode.HALF_UP);
            index++;
        }

        return symbol + (negative ? "-" : "") + formatDecimal(rounded) + SUFFIXES[index];
    }

    private static String formatDecimal(BigDecimal value) {
        return value
                .stripTrailingZeros()
                .toPlainString();
    }
}

