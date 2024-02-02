package ru.practicum.error.exceptions;

public class ServiceConnection extends RuntimeException {
    public ServiceConnection(String message) {
        super(message);
    }
}
