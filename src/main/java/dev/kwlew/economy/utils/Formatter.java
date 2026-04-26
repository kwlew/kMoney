package dev.kwlew.economy.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
