package ru.practicum.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class EventShortDto {
    private final Long id;
    private final String title;
    private final String annotation;
    private final UserShortDto initiator;
    private final CategoryDto category;
    private final LocalDateTime eventDate;
    private final Boolean paid;
    private final Integer confirmedRequests;
    private final Long views;
}
