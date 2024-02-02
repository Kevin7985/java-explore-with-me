package ru.practicum.request.exceptions;

public class RequestConflict extends RuntimeException {
    public RequestConflict(String message) {
        super(message);
    }
}
