package dev.kwlew.managers.exceptions;

public class CircularDependencyException extends RuntimeException {
    public CircularDependencyException(String cycle) {
        super("Circular dependency detected: " + cycle);
    }
}
