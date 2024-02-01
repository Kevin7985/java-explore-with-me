package ru.practicum.event;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.StatsClient;
import ru.practicum.dto.EndpointHit;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.EventSort;
import ru.practicum.event.model.EventState;
import ru.practicum.request.dto.ParticipationRequestDto;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class EventController {
    private final EventService eventService;

    private final StatsClient statsClient;

    @GetMapping("/users/{userId}/events")
    public List<EventFullDto> getEventsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        return eventService.getEventsByUserId(userId, from, size);
    }

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable Long userId, @RequestBody @Valid NewEventDto eventDto) {
        return eventService.createEvent(userId, eventDto);
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public EventFullDto getEventByIdAndUser(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.getEventByIdAndUser(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId, @PathVariable Long eventId, @RequestBody @Valid UpdateEventUserRequest eventDto) {
        return eventService.updateEvent(userId, eventId, eventDto);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.getEventRequests(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestsStatus(@PathVariable Long userId, @PathVariable Long eventId, @RequestBody EventRequestStatusUpdateRequest request) {
        return eventService.updateRequestsStatus(userId, eventId, request);
    }

    @GetMapping("/admin/events")
    public List<EventFullDto> searchEventsAdmin(
            HttpServletRequest req,
            @RequestParam(defaultValue = "") List<Long> users,
            @RequestParam(defaultValue = "") List<EventState> states,
            @RequestParam(defaultValue = "") List<Long> categories,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "10") @Min(1) Integer size
            ) {

        LocalDateTime start = rangeStart == null ? null : LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime end = rangeEnd == null ? null : LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return eventService.searchEventsAdmin(users, states, categories, start, end, from, size);
    }

    @PatchMapping("/admin/events/{eventId}")
    public EventFullDto updateEventByAdmin(@PathVariable Long eventId, @RequestBody UpdateEventAdminRequest eventAdminRequest) {
        return eventService.updateEventByAdmin(eventId, eventAdminRequest);
    }

    @GetMapping("/events")
    public List<EventFullDto> searchEvents(
            HttpServletRequest req,
            @RequestParam(required = false) String text,
            @RequestParam(defaultValue = "") List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(required = false) Boolean onlyAvailable,
            @RequestParam(defaultValue = "ID") EventSort sort,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "10") @Min(1) Integer size
            ) {
        statsClient.saveEndpoint(new EndpointHit(
                "ewm-main-service",
                "/events",
                req.getRemoteAddr(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));

        LocalDateTime start = rangeStart == null ? null : LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime end = rangeEnd == null ? null : LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return eventService.searchEvents(text, categories, paid, start, end, onlyAvailable, sort, from, size);
    }

    @GetMapping("/events/{id}")
    public EventFullDto getEventById(@PathVariable Long id, HttpServletRequest req) {
        EventFullDto event = eventService.getEventById(id);

        statsClient.saveEndpoint(new EndpointHit(
                "ewm-main-service",
                "/events/" + id,
                req.getRemoteAddr(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));

        return event;
    }
}
