package dev.kwlew.kmoney.hooks.vault;

import dev.kwlew.kmoney.managers.config.ConfigManager;

final class VaultCurrency {

    private static final String DEFAULT_CURRENCY_NAME = "Money";

    private VaultCurrency() {
    }

    static String resolveCurrencyName(ConfigManager config) {
        String symbol = config.getCurrencySymbol();
        if (symbol != null) {
            String trimmed = symbol.trim();
            if (!trimmed.isEmpty() && trimmed.chars().allMatch(Character::isLetter)) {
                return trimmed;
            }
        }

        return DEFAULT_CURRENCY_NAME;
    }

    static String pluralize(String name) {
        if (name.endsWith("s")) {
            return name;
        }

        return name + "s";
    }
}