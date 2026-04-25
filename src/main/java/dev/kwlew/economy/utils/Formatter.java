package dev.kwlew.economy.utils;

public class Formatter {
    private static final String[] SUFFIXES = {"", "K", "M", "B", "T", "Q", "QQ", "S", "SS"};

    public static String format(double amount, String symbol) {

        int index = 0;

        while (amount >= 1000 && index < SUFFIXES.length-1) {
            amount /= 1000;
            index++;
        }

        if (amount >= 999.5 && index < SUFFIXES.length - 1) {
            amount /= 1000;
            index++;
        }

        return symbol + formatDecimal(amount) + SUFFIXES[index];
    }

    private static String formatDecimal(double value) {
        return String.format("%.1f", value)
                .replace(".0", "");
    }
}
