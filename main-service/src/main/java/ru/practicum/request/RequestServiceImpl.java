package ru.practicum.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.feed.FeedRepository;
import ru.practicum.feed.model.Feed;
import ru.practicum.feed.model.FeedType;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.exceptions.RequestConflict;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.service.MapperService;
import ru.practicum.service.ValidationService;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final MapperService mapperService;
    private final ValidationService validationService;
    private final RequestRepository requestRepository;
    private final FeedRepository feedRepository;

    @Override
    public List<ParticipationRequestDto> getRequests(Long userId) {
        validationService.validateUser(userId);

        List<Request> requests = requestRepository.findByRequesterId(userId);
        return requests.stream()
                .map(mapperService::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User user = validationService.validateUser(userId);
        Event event = validationService.validateEvent(eventId);

        Optional<Request> found = requestRepository.findByRequesterIdAndEventId(userId, eventId);
        if (found.isPresent() && !found.get().getStatus().equals(RequestStatus.CANCELED)) {
            throw new RequestConflict("Заявка на данное событие уже была отправлена");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new RequestConflict("Инициатор события не может подать заявку на участие в своём же мероприятии");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new RequestConflict("Невозможно подать заявку на участие в не неопубликованном событии");
        }

        List<Request> participants = requestRepository.findByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() != 0 && participants.size() == event.getParticipantLimit()) {
            throw new RequestConflict("Количество участников мероприятия достигло максимального количества");
        }

        Request request = new Request(
                found.map(Request::getId).orElse(null),
                userId,
                eventId,
                event.getRequestModeration() && event.getParticipantLimit() != 0 ? RequestStatus.PENDING : RequestStatus.CONFIRMED,
                LocalDateTime.now()
        );

        if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
            feedRepository.save(new Feed(
                    null,
                    user,
                    FeedType.PARTICIPATE,
                    eventId,
                    LocalDateTime.now()
            ));
        }

        return mapperService.toRequestDto(requestRepository.save(request));
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        validationService.validateUser(userId);
        Request request = validationService.validateRequest(requestId);

        request.setStatus(RequestStatus.CANCELED);

        Optional<Feed> foundFeed = feedRepository.findByUser_IdAndFeedTypeAndEntityId(
                userId,
                FeedType.PARTICIPATE,
                request.getEventId()
        );

        foundFeed.ifPresent(feed -> feedRepository.deleteById(feed.getId()));

        return mapperService.toRequestDto(requestRepository.save(request));
    }
}
