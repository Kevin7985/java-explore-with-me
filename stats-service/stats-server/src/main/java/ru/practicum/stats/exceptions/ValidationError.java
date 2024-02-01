package ru.practicum.stats.exceptions;

public class ValidationError extends RuntimeException {
    public ValidationError(String message) {
        super(message);
    }
}
