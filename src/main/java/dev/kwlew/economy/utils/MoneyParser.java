package dev.kwlew.economy.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.math.BigDecimal;

/**
 * Parses money amounts with support for common suffixes.
 * <p>
 * Supported suffixes (case-insensitive):
 * - k: 1,000 (thousand)
 * - m: 1,000,000 (million)
 * - b: 1,000,000,000 (billion)
 * - t: 1,000,000,000,000 (trillion)
 * - q: 1,000,000,000,000,000 (quadrillion)
 * - qq: 1,000,000,000,000,000,000 (quintillion)
 * - s: 1,000,000,000,000,000,000,000 (sextillion)
 * - ss: 1,000,000,000,000,000,000,000,000 (septillion)
 * - oc: 1,000,000,000,000,000,000,000,000,000 (octillion)
 * - no: 1,000,000,000,000,000,000,000,000,000,000 (nonillion)
 * - dc: 1,000,000,000,000,000,000,000,000,000,000,000 (decillion)
 * - udc: 1,000,000,000,000,000,000,000,000,000,000,000,000 (undecillion)
 * <p>
 * Examples: "100", "1k", "5.5m", "2.5b", "1,000"
 */
public class MoneyParser {

    private static final BigDecimal THOUSAND = new BigDecimal("1000");
    private static final Map<String, BigDecimal> SUFFIXES = new LinkedHashMap<>();

    static {
        SUFFIXES.put("udc", THOUSAND.pow(12)); // 1e36
        SUFFIXES.put("dc",  THOUSAND.pow(11)); // 1e33
        SUFFIXES.put("no",  THOUSAND.pow(10)); // 1e30
        SUFFIXES.put("oc",  THOUSAND.pow(9)); // 1e27
        SUFFIXES.put("ss", THOUSAND.pow(8)); // 1e24
        SUFFIXES.put("s",  THOUSAND.pow(7)); // 1e21
        SUFFIXES.put("qq", THOUSAND.pow(6)); // 1e18
        SUFFIXES.put("q",  THOUSAND.pow(5)); // 1e15
        SUFFIXES.put("t",  THOUSAND.pow(4)); // 1e12
        SUFFIXES.put("b",  THOUSAND.pow(3)); // 1e9
        SUFFIXES.put("m",  THOUSAND.pow(2)); // 1e6
        SUFFIXES.put("k",  THOUSAND); // 1e3
    }

    /**
     * Parses a money amount string with optional suffix support.
     *
     * @param input the string to parse (e.g., "100", "1k", "5.5m")
     * @return the parsed amount as BigDecimal
     * @throws NumberFormatException if the input format is invalid
     */
    public static BigDecimal parse(String input) throws NumberFormatException {

        if (input == null || input.isEmpty()) {
            throw new NumberFormatException("Empty input");
        }

        input = input.toLowerCase().replace(",", "").trim();
        input = input.replaceAll("[^0-9a-z.]", "");

        if (input.isEmpty()) {
            throw new NumberFormatException("Invalid input");
        }

        for (Map.Entry<String, BigDecimal> entry : SUFFIXES.entrySet()) {
            String suffix = entry.getKey();

            if (input.endsWith(suffix)) {
                String numberPart = input.substring(0, input.length() - suffix.length());

                if (numberPart.isEmpty()) {
                    throw new NumberFormatException("Invalid number format");
                }

                BigDecimal value = new BigDecimal(numberPart);
                return value.multiply(entry.getValue());
            }
        }

        return new BigDecimal(input);
    }
}
