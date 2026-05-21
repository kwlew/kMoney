package dev.kwlew.kmoney.economy.api;

import java.math.BigDecimal;
import java.util.UUID;

public record EconomyTopEntry(UUID uuid, BigDecimal balance) {
}
