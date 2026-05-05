package dev.kwlew.kmoney.managers.exceptions;

import java.util.UUID;

public class PlayerSaveException extends RuntimeException {
    public PlayerSaveException(UUID uuid) {
        super("Failed to save player " + uuid);
    }
}