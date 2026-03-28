package me.kwlew.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class MoneyFormatter {

    private static final NumberFormat format = NumberFormat.getInstance(Locale.US);

    public static String format(int amount) {
        return format.format(amount);
    }

}
