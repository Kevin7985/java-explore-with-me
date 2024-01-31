package ru.practicum.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.event.model.EventState;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.category.dto.CategoryDto;

@Data
@AllArgsConstructor
public class EventFullDto {
    private final Long id;
    private final String title;
    private final String annotation;
    private final String description;
    private final CategoryDto category;
    private final UserShortDto initiator;
    private final Integer confirmedRequests;
    private final String createdOn;
    private final String eventDate;
    private final Location location;
    private final Boolean paid;
    private final Integer participantLimit;
    private final String publishedOn;
    private final Boolean requestModeration;
    private final EventState state;
    private final Long views;
}
