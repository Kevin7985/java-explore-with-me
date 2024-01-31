package ru.practicum.event.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.event.dto.Location;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UpdateEvent {
    private final String title;
    private final String annotation;
    private final String description;
    private final Long categoryId;
    private final LocalDateTime eventDate;
    private final Boolean paid;
    private final Integer participantLimit;
    private Boolean requestModeration;
    private Location location;
}
