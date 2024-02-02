package ru.practicum.request;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findByEventIdInAndStatus(List<Long> eventId, RequestStatus status);

    Optional<Request> findByRequesterIdAndEventId(Long userId, Long eventId);

    List<Request> findByRequesterId(Long userId);

    List<Request> findByEventId(Long eventId);

    List<Request> findByEventIdAndIdIn(Long eventId, List<Long> ids);
}
