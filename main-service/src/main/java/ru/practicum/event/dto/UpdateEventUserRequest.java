package ru.practicum.event.dto;

import lombok.Data;
import ru.practicum.event.exceptions.EventValidation;

import java.time.LocalDateTime;

@Data
public class UpdateEventUserRequest {
    private final String title;
    private final String annotation;
    private final String description;
    private final Long categoryId;
    private final LocalDateTime eventDate;
    private final Boolean paid;
    private final Integer participantLimit;
    private Boolean requestModeration;
    private Location location;
    private final StateActionUser stateAction;

    public UpdateEventUserRequest(String title, String annotation, String description, Long category, LocalDateTime eventDate, Boolean paid, Integer participantLimit, Boolean requestModeration, Location location, StateActionUser stateAction) {
        if (title != null && (title.length() < 3 || title.length() > 120)) {
            throw new EventValidation("размер title должен находиться в диапазоне от 3 до 120");
        }
        this.title = title;

        if (annotation != null && (annotation.length() < 20 || annotation.length() > 2000)) {
            throw new EventValidation("размер annotation должен находиться в диапазоне от 20 до 2000");
        }
        this.annotation = annotation;

        if (description != null && (description.length() < 20 || description.length() > 7000)) {
            throw new EventValidation("размер annotation должен находиться в диапазоне от 20 до 7000");
        }
        this.description = description;

        this.categoryId = category;
        this.eventDate = eventDate;
        this.paid = paid;

        if (participantLimit != null && participantLimit < 0) {
            throw new EventValidation("participationLimit должен быть не менее 0");
        }
        this.participantLimit = participantLimit;

        this.requestModeration = requestModeration;
        this.location = location;
        this.stateAction = stateAction;
    }
}
