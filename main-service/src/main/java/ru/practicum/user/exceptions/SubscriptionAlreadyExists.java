package ru.practicum.user.exceptions;

public class SubscriptionAlreadyExists extends RuntimeException {
    public SubscriptionAlreadyExists(String message) {
        super(message);
    }
}
