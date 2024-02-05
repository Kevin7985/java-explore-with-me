package ru.practicum.user.exceptions;

public class SubscriptionConflict extends RuntimeException {
    public SubscriptionConflict(String message) {
        super(message);
    }
}
