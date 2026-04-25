package dev.kwlew.economy.utils;

import java.util.LinkedHashMap;
import java.util.Map;

// Dont touch this broski
public class MoneyParser {

    private static final Map<String, Double> SUFFIXES = new LinkedHashMap<>();

    static {
        SUFFIXES.put("ss", Math.pow(1000, 8)); // 1e24
        SUFFIXES.put("s",  Math.pow(1000, 7)); // 1e21
        SUFFIXES.put("qq", Math.pow(1000, 6)); // 1e18
        SUFFIXES.put("q",  Math.pow(1000, 5)); // 1e15
        SUFFIXES.put("t",  Math.pow(1000, 4)); // 1e12
        SUFFIXES.put("b",  Math.pow(1000, 3)); // 1e9
        SUFFIXES.put("m",  Math.pow(1000, 2)); // 1e6
        SUFFIXES.put("k",  Math.pow(1000, 1)); // 1e3
    }

    public static double parse(String input) throws NumberFormatException {

        if (input == null || input.isEmpty()) {
            throw new NumberFormatException("Empty input");
        }

        input = input.toLowerCase().replace(",", "").trim();
        input = input.replaceAll("[^0-9a-z.]", "");

        if (input.isEmpty()) {
            throw new NumberFormatException("Invalid input");
        }

        for (Map.Entry<String, Double> entry : SUFFIXES.entrySet()) {
            String suffix = entry.getKey();

            if (input.endsWith(suffix)) {
                String numberPart = input.substring(0, input.length() - suffix.length());

                if (numberPart.isEmpty()) {
                    throw new NumberFormatException("Invalid number format");
                }

                double value = Double.parseDouble(numberPart);
                return value * entry.getValue();
            }
        }

        return Double.parseDouble(input);
    }
}
