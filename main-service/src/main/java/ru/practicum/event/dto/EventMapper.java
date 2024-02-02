package ru.practicum.event.dto;

import org.springframework.stereotype.Component;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class EventMapper {
    public Event toEvent(NewEventDto eventDto, Category category, User initiator) {
        return new Event(
                null,
                eventDto.getTitle(),
                eventDto.getAnnotation(),
                eventDto.getDescription(),
                eventDto.getEventDate(),
                eventDto.getLocation().getLat(),
                eventDto.getLocation().getLon(),
                category,
                initiator,
                eventDto.getPaid(),
                eventDto.getParticipantLimit(),
                eventDto.getRequestModeration(),
                EventState.PENDING,
                LocalDateTime.now(),
                null
        );
    }

    public EventFullDto toEventDto(Event event,
                                   CategoryDto categoryDto,
                                   UserShortDto initiatorDto,
                                   Integer confirmedRequests,
                                   Long views) {
        return new EventFullDto(
                event.getId(),
                event.getTitle(),
                event.getAnnotation(),
                event.getDescription(),
                categoryDto,
                initiatorDto,
                confirmedRequests,
                event.getCreatedOn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                event.getEventDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                new Location(event.getLatitude(), event.getLongitude()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getPublishedOn() == null ? null : event.getPublishedOn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                event.getRequestModeration(),
                event.getState(),
                views
        );
    }

    public EventShortDto toEventShotDto(Event event,
                                    CategoryDto categoryDto,
                                    UserShortDto initiatorDto,
                                    Integer confirmedRequests,
                                    Long views) {
        return new EventShortDto(
                event.getId(),
                event.getTitle(),
                event.getAnnotation(),
                initiatorDto,
                categoryDto,
                event.getEventDate(),
                event.getPaid(),
                confirmedRequests,
                views
        );
    }
}
