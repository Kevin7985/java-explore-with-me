package ru.practicum.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.request.model.RequestStatus;

import java.util.List;

@Data
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {
    private final List<Long> requestIds;
    private final RequestStatus status;
}
