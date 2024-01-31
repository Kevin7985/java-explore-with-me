package ru.practicum.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.request.model.RequestStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ParticipationRequestDto {
    private final Long id;
    private final Long requester;
    private final Long event;
    private final RequestStatus status;
    private final LocalDateTime created;
}
