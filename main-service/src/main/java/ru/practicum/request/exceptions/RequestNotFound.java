package ru.practicum.request.exceptions;

public class RequestNotFound extends RuntimeException {
    public RequestNotFound(String message) {
        super(message);
    }
}
