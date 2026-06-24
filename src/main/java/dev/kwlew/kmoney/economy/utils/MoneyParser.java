package dev.kwlew.kmoney.economy.utils;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * - ddc: 1e39 (duodecillion)
 * - tdc: 1e42 (tredecillion)
 * - qtdc: 1e45 (quattuordecillion)
 * - qndc: 1e48 (quindecillion)
 * - sxdc: 1e51 (sexdecillion)
 * - spdc: 1e54 (septendecillion)
 * - ocdc: 1e57 (octodecillion)
 * - nodc: 1e60 (novemdecillion)
 * - vg: 1e63 (vigintillion)
 * - uvg: 1e66 (unvigintillion)
 * - dvg: 1e69 (duovigintillion)
 * - tvg: 1e72 (trevigintillion)
 * <p>
 * Examples: "100", "1k", "5.5m", "2.5b", "1,000"
 */
public class MoneyParser {

    private static final BigDecimal THOUSAND = new BigDecimal("1000");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "^(?<number>(?:\\d{1,3}(?:,\\d{3})+|\\d+)(?:\\.\\d+)?)(?<suffix>[a-z]{0,4})$"
    );
    private static final Map<String, BigDecimal> SUFFIXES = new LinkedHashMap<>();

    static {
        SUFFIXES.put("tvg", THOUSAND.pow(24)); // 1e72
        SUFFIXES.put("dvg", THOUSAND.pow(23)); // 1e69
        SUFFIXES.put("uvg", THOUSAND.pow(22)); // 1e66
        SUFFIXES.put("vg",  THOUSAND.pow(21)); // 1e63
        SUFFIXES.put("nodc", THOUSAND.pow(20)); // 1e60
        SUFFIXES.put("ocdc", THOUSAND.pow(19)); // 1e57
        SUFFIXES.put("spdc", THOUSAND.pow(18)); // 1e54
        SUFFIXES.put("sxdc", THOUSAND.pow(17)); // 1e51
        SUFFIXES.put("qndc", THOUSAND.pow(16)); // 1e48
        SUFFIXES.put("qtdc", THOUSAND.pow(15)); // 1e45
        SUFFIXES.put("tdc", THOUSAND.pow(14)); // 1e42
        SUFFIXES.put("ddc", THOUSAND.pow(13)); // 1e39
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

        if (input == null || input.trim().isEmpty()) {
            throw new NumberFormatException("Empty input");
        }

        String normalized = input.trim().toLowerCase(Locale.ROOT);
        Matcher matcher = AMOUNT_PATTERN.matcher(normalized);

        if (!matcher.matches()) {
            throw new NumberFormatException("Invalid input");
        }

        String numberPart = matcher.group("number").replace(",", "");
        String suffix = matcher.group("suffix");

        BigDecimal value = new BigDecimal(numberPart);
        if (suffix.isEmpty()) {
            return value;
        }

        BigDecimal multiplier = SUFFIXES.get(suffix);
        if (multiplier == null) {
            throw new NumberFormatException("Unknown suffix: " + suffix);
        }

        return value.multiply(multiplier);
    }
}
