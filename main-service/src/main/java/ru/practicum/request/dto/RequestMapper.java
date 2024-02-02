package ru.practicum.request.dto;

import org.springframework.stereotype.Component;
import ru.practicum.request.model.Request;

@Component
public class RequestMapper {
    public ParticipationRequestDto toRequestDto(Request request) {
        return new ParticipationRequestDto(
                request.getId(),
                request.getRequesterId(),
                request.getEventId(),
                request.getStatus(),
                request.getCreated()
        );
    }
}
