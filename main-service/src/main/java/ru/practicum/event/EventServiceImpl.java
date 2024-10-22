package ru.practicum.event;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.StatsClient;
import ru.practicum.category.model.Category;
import ru.practicum.dto.ViewStats;
import ru.practicum.error.exceptions.ServiceConnection;
import ru.practicum.event.dto.*;
import ru.practicum.event.exceptions.DateValidation;
import ru.practicum.event.exceptions.EventConditions;
import ru.practicum.event.exceptions.EventNotFound;
import ru.practicum.event.exceptions.EventValidation;
import ru.practicum.event.model.*;
import ru.practicum.feed.FeedRepository;
import ru.practicum.feed.model.Feed;
import ru.practicum.feed.model.FeedType;
import ru.practicum.request.RequestRepository;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.exceptions.RequestConflict;
import ru.practicum.request.exceptions.RequestNotFound;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.service.MapperService;
import ru.practicum.service.ValidationService;
import ru.practicum.user.UserRepository;
import ru.practicum.user.model.User;
import ru.practicum.utils.Pagination;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final EntityManager entityManager;

    private final MapperService mapperService;
    private final ValidationService validationService;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final FeedRepository feedRepository;

    private final StatsClient statsClient;

    @Override
    public List<EventFullDto> getEventsByUserId(Long userId, Integer from, Integer size) {
        List<EventFullDto> events = new ArrayList<>();

        Pageable pageable;
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Page<Event> page;
        Pagination pager = new Pagination(from, size);

        if (size == null) {
            pageable = PageRequest.of(pager.getPageStart(), pager.getPageSize(), sort);
            page = eventRepository.findByInitiator_Id(userId, pageable);

            while (page.hasContent()) {
                events.addAll(toEventDto(page.toList()));
                pageable = pageable.next();
                page = eventRepository.findByInitiator_Id(userId, pageable);
            }
        } else {
            for (int i = pager.getPageStart(); i < pager.getPagesAmount(); i++) {
                pageable = PageRequest.of(i, pager.getPageSize(), sort);
                page = eventRepository.findByInitiator_Id(userId, pageable);
                events.addAll(toEventDto(page.toList()));
            }

            events = events.stream().limit(size).collect(Collectors.toList());
        }

        log.info("Получен список всех событий созданных текущим пользователем");
        return events;
    }

    @Override
    public EventFullDto createEvent(Long userId, NewEventDto eventDto) {
        User user = validationService.validateUser(userId);
        Category category = validationService.validateCategory(eventDto.getCategoryId());

        if (eventDto.getPaid() == null) {
            eventDto.setPaid(false);
        }

        if (eventDto.getParticipantLimit() == null) {
            eventDto.setParticipantLimit(0);
        }

        if (eventDto.getRequestModeration() == null) {
            eventDto.setRequestModeration(true);
        }

        LocalDateTime startDate = LocalDateTime.now().plusHours(2);
        if (eventDto.getEventDate().isBefore(startDate)) {
            throw new DateValidation("Начало события не может быть раньше чем через 2 часа от текущего момента");
        }

        Event event = mapperService.toEvent(eventDto, category, user);

        log.info("Создано новое событие: " + event);
        return mapperService.toEventDto(eventRepository.save(event), 0, 0L);
    }

    @Override
    public EventFullDto getEventByIdAndUser(Long userId, Long eventId) {
        validationService.validateUser(userId);
        Event event = validationService.validateEvent(eventId);

        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new EventNotFound("Событие с id = " + eventId + " не найдено");
        }

        log.info("Получено событие: " + event);
        return toEventDto(event);
    }

    @Override
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest eventDto) {
        validationService.validateUser(userId);
        Event event = validationService.validateEvent(eventId);

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new EventConditions("Обновляемое событие не должно быть опубликовано");
        }

        LocalDateTime startDate = LocalDateTime.now().plusHours(2);
        if (eventDto.getEventDate() != null && eventDto.getEventDate().isBefore(startDate)) {
            throw new DateValidation("Начало события не может быть раньше чем через 2 часа от текущего момента");
        }

        updateEventData(event, new UpdateEvent(
                eventDto.getTitle(),
                eventDto.getAnnotation(),
                eventDto.getDescription(),
                eventDto.getCategoryId(),
                eventDto.getEventDate(),
                eventDto.getPaid(),
                eventDto.getParticipantLimit(),
                eventDto.getRequestModeration(),
                eventDto.getLocation()
        ));

        if (eventDto.getStateAction() != null && eventDto.getStateAction().equals(StateActionUser.CANCEL_REVIEW)) {
            event.setState(EventState.CANCELED);
        } else if (eventDto.getStateAction() != null && eventDto.getStateAction().equals(StateActionUser.SEND_TO_REVIEW)) {
            event.setState(EventState.PENDING);
        }

        log.info("Обновлено событие (пользователь): " + event);
        return toEventDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest eventDto) {
        Event event = validationService.validateEvent(eventId);

        if (eventDto.getEventDate() != null && eventDto.getEventDate().isBefore(LocalDateTime.now())) {
            throw new EventValidation("Дата начала события не может быть в прошлом");
        }

        if (eventDto.getEventDate() != null && event.getPublishedOn() != null) {
            LocalDateTime startTime = event.getPublishedOn().minusHours(1);
            if (eventDto.getEventDate().isBefore(startTime)) {
                throw new EventConditions("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
            }
        }

        updateEventData(event, new UpdateEvent(
                eventDto.getTitle(),
                eventDto.getAnnotation(),
                eventDto.getDescription(),
                eventDto.getCategoryId(),
                eventDto.getEventDate(),
                eventDto.getPaid(),
                eventDto.getParticipantLimit(),
                eventDto.getRequestModeration(),
                eventDto.getLocation()
        ));

        if (eventDto.getStateAction() != null && eventDto.getStateAction().equals(StateActionAdmin.PUBLISH_EVENT)) {
            if (event.getState().equals(EventState.PENDING)) {
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else {
                throw new EventConditions("Событие можно публиковать, только если оно в состоянии ожидания публикации");
            }
        } else if (eventDto.getStateAction() != null && eventDto.getStateAction().equals(StateActionAdmin.REJECT_EVENT)) {
            if (!event.getState().equals(EventState.PUBLISHED)) {
                event.setState(EventState.CANCELED);
            } else {
                throw new EventConditions("Событие можно отклонить, только если оно еще не опубликовано");
            }
        }

        if (eventDto.getStateAction() != null && event.getState().equals(EventState.PUBLISHED)) {
            feedRepository.save(new Feed(
                    null,
                    event.getInitiator(),
                    FeedType.CREATE_NEW_EVENT,
                    eventId,
                    LocalDateTime.now()
            ));
        }

        log.info("Обновлено событие (админ): " + event);
        return toEventDto(eventRepository.save(event));
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        validationService.validateUser(userId);
        Event event = validationService.validateEvent(eventId);

        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new EventNotFound("Событие с id = " + eventId + " не найдено");
        }

        log.info("Получены запросы на участие");
        return requestRepository.findByEventId(eventId).stream()
                .map(mapperService::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestsStatus(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequest request) {

        validationService.validateUser(userId);
        Event event = validationService.validateEvent(eventId);

        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new EventNotFound("Событие с id = " + eventId + " не найдено");
        }

        List<Request> confirmedRequests = requestRepository.findByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() != 0 && confirmedRequests.size() >= event.getParticipantLimit()) {
            throw new RequestConflict("В событии уже набралось максимальное количество участников");
        }

        if (!event.getRequestModeration()) {
            List<Request> requestsPending = requestRepository.findByEventIdAndStatus(event.getId(), RequestStatus.PENDING);

            List<Request> toAdd = new ArrayList<>();
            List<Long> confirmedUserIds = new ArrayList<>();
            requestsPending.forEach(item -> {
                if (event.getParticipantLimit() == 0 || confirmedRequests.size() < event.getParticipantLimit()) {
                    item.setStatus(RequestStatus.CONFIRMED);
                    confirmedRequests.add(item);

                    confirmedUserIds.add(item.getRequesterId());
                } else {
                    item.setStatus(RequestStatus.REJECTED);
                }

                toAdd.add(item);
            });

            requestRepository.saveAll(toAdd);

            List<User> users = userRepository.findByIdIn(confirmedUserIds);
            List<Feed> feedList = new ArrayList<>();
            users.forEach(item -> feedList.add(new Feed(
                    null,
                    item,
                    FeedType.PARTICIPATE,
                    eventId,
                    LocalDateTime.now()
            )));

            feedRepository.saveAll(feedList);

            return new EventRequestStatusUpdateResult(
                    confirmedRequests.stream()
                            .map(mapperService::toRequestDto).collect(Collectors.toList()),
                    new ArrayList<>()
            );
        }

        List<Request> foundRequests = requestRepository.findByEventIdAndIdIn(eventId, request.getRequestIds());
        HashMap<Long, Request> reqs = new HashMap<>();
        foundRequests.forEach(item -> {
            reqs.put(item.getId(), item);
        });

        List<Long> foundRequestIds = foundRequests.stream()
                .map(Request::getId)
                .collect(Collectors.toList());

        List<Request> requestsToSave = new ArrayList<>();
        List<Long> confirmedUserIds = new ArrayList<>();

        request.getRequestIds().forEach(item -> {
            if (!foundRequestIds.contains(item)) {
                throw new RequestNotFound("Заявка с id = " + item + " не найдена");
            }

            Request req = reqs.get(item);
            if (req.getStatus().equals(RequestStatus.CANCELED)) {
                throw new RequestConflict("Принять можно только заявки, которые ожидают решения");
            }

            req.setStatus(request.getStatus());

            if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
                if (event.getParticipantLimit() != 0 && confirmedRequests.size() >= event.getParticipantLimit()) {
                    req.setStatus(RequestStatus.REJECTED);
                } else {
                    confirmedRequests.add(req);
                    confirmedUserIds.add(req.getRequesterId());
                }
            }

            requestsToSave.add(req);
        });

        requestRepository.saveAll(requestsToSave);

        List<User> users = userRepository.findByIdIn(confirmedUserIds);
        List<Feed> feedList = new ArrayList<>();
        users.forEach(item -> feedList.add(new Feed(
                null,
                item,
                FeedType.PARTICIPATE,
                eventId,
                LocalDateTime.now()
        )));

        feedRepository.saveAll(feedList);

        log.info("Обновлены статусы запросов на участие");
        return new EventRequestStatusUpdateResult(
                confirmedRequests.stream()
                        .map(mapperService::toRequestDto)
                        .collect(Collectors.toList()),
                requestRepository.findByEventIdAndStatus(eventId, RequestStatus.REJECTED).stream()
                        .map(mapperService::toRequestDto)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public List<EventFullDto> searchEventsAdmin(List<Long> userIds, List<EventState> states, List<Long> categoryIds, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        QEvent qEvent = QEvent.event;

        List<BooleanExpression> params = new ArrayList<>();

        if (!userIds.isEmpty()) {
            params.add(qEvent.initiator.id.in(userIds));
        }

        if (!states.isEmpty()) {
            params.add(qEvent.state.in(states));
        }

        if (!categoryIds.isEmpty()) {
            params.add(qEvent.category.id.in(categoryIds));
        }

        if (rangeStart != null) {
            params.add(qEvent.eventDate.after(rangeStart));
        }

        if (rangeEnd != null) {
            params.add(qEvent.eventDate.before(rangeEnd));
        }

        BooleanExpression query = Expressions.allOf(params.toArray(new BooleanExpression[params.size()]));

        List<EventFullDto> events = new ArrayList<>();

        Pageable pageable;
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Page<Event> page;
        Pagination pager = new Pagination(from, size);

        List<Event> eventsTemp = new ArrayList<>();

        if (size == null) {
            pageable = PageRequest.of(pager.getPageStart(), pager.getPageSize(), sort);
            page = query != null ? eventRepository.findAll(query, pageable) : eventRepository.findAll(pageable);

            while (page.hasContent()) {
                eventsTemp.addAll(page.toList());
                pageable = pageable.next();
                page = query != null ? eventRepository.findAll(query, pageable) : eventRepository.findAll(pageable);
            }

            events.addAll(eventsTemp.stream().map(this::toEventDto).collect(Collectors.toList()));
        } else {
            for (int i = pager.getPageStart(); i < pager.getPagesAmount(); i++) {
                pageable = PageRequest.of(i, pager.getPageSize(), sort);
                page = query != null ? eventRepository.findAll(query, pageable) : eventRepository.findAll(pageable);
                eventsTemp.addAll(page.toList());
            }

            events.addAll(eventsTemp.stream().limit(size).map(this::toEventDto).collect(Collectors.toList()));
        }

        log.info("Поиск событий (админ)");
        return events;
    }

    @Override
    public EventFullDto getEventById(Long id) {
        Event event = validationService.validateEvent(id);

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new EventNotFound("Событие с id = " + id + " не найдено (учитываются только опубликованные события)");
        }

        return toEventDto(event);
    }

    @Override
    public List<EventFullDto> searchEvents(String text, List<Long> categoryIds, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, EventSort sort, Integer from, Integer size) {
        QEvent qevent = QEvent.event;

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        JPQLQuery<Event> query = queryFactory.selectFrom(qevent).where(qevent.state.eq(EventState.PUBLISHED));

        if (text != null) {
            query.where(qevent.annotation.toLowerCase().contains(text.toLowerCase()).or(qevent.description.toLowerCase().contains(text.toLowerCase())));
        }

        if (!categoryIds.isEmpty()) {
            query.where(qevent.category.id.in(categoryIds));
        }

        if (paid != null) {
            query.where(qevent.paid.eq(paid));
        }

        if (rangeStart == null && rangeEnd == null) {
            query.where(qevent.eventDate.after(LocalDateTime.now()));
        } else {
            if (rangeStart != null && rangeEnd != null) {
                if (rangeEnd.isBefore(rangeStart)) {
                    throw new EventValidation("rangeEnd не может быть раньше rangeStart");
                }
            }

            if (rangeStart != null) {
                query.where(qevent.eventDate.after(rangeStart));
            }

            if (rangeEnd != null) {
                query.where(qevent.eventDate.before(rangeEnd));
            }
        }

        if (sort.equals(EventSort.ID)) {
            query.orderBy(qevent.id.asc());
        } else if (sort.equals(EventSort.EVENT_DATE)) {
            query.orderBy(qevent.eventDate.asc());
        }

        Stream<EventFullDto> events = query.fetch().stream()
                .map(this::toEventDto)
                .filter(item -> {
                    if (onlyAvailable != null && onlyAvailable) {
                        return item.getParticipantLimit() == 0 || item.getConfirmedRequests() < item.getParticipantLimit();
                    } else {
                        return true;
                    }
                });

        if (sort.equals(EventSort.VIEWS)) {
            events = events.sorted(new Comparator<EventFullDto>() {
                @Override
                public int compare(EventFullDto o1, EventFullDto o2) {
                    return (int) (o1.getViews() - o2.getViews());
                }
            });
        }

        events = events.skip(from).limit(size);

        return events.collect(Collectors.toList());
    }

    private void updateEventData(Event event, UpdateEvent updateEvent) {
        event.setTitle(updateEvent.getTitle() == null ? event.getTitle() : updateEvent.getTitle());
        event.setAnnotation(updateEvent.getAnnotation() == null ? event.getAnnotation() : updateEvent.getAnnotation());
        event.setDescription(updateEvent.getDescription() == null ? event.getDescription() : updateEvent.getDescription());

        if (updateEvent.getCategoryId() != null) {
            Category category = validationService.validateCategory(updateEvent.getCategoryId());
            event.setCategory(category);
        }

        event.setEventDate(updateEvent.getEventDate() == null ? event.getEventDate() : updateEvent.getEventDate());
        event.setPaid(updateEvent.getPaid() == null ? event.getPaid() : updateEvent.getPaid());
        event.setParticipantLimit(updateEvent.getParticipantLimit() == null ? event.getParticipantLimit() : updateEvent.getParticipantLimit());
        event.setRequestModeration(updateEvent.getRequestModeration() == null ? event.getRequestModeration() : updateEvent.getRequestModeration());
        event.setLatitude(updateEvent.getLocation() == null ? event.getLatitude() : updateEvent.getLocation().getLat());
        event.setLongitude(updateEvent.getLocation() == null ? event.getLongitude() : updateEvent.getLocation().getLon());
    }

    public List<EventFullDto> toEventDto(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());

        List<Request> requests = requestRepository.findByEventIdInAndStatus(eventIds, RequestStatus.CONFIRMED);
        HashMap<Long, List<Request>> req = new HashMap<>();

        requests.forEach(item -> {
            if (!req.containsKey(item.getId())) {
                req.put(item.getId(), new ArrayList<>());
            }

            req.get(item.getId()).add(item);
        });

        List<String> uris = eventIds.stream().map(item -> "/events/" + item).collect(Collectors.toList());
        List<ViewStats> statsReq = statsClient.getStats(LocalDateTime.of(2024, 1, 1, 0, 0, 0), LocalDateTime.now(), uris, true);
        if (statsReq == null) {
            throw new ServiceConnection("Возвращён пустой ответ сервера статистики");
        }

        HashMap<Long, ViewStats> stats = new HashMap<>();
        statsReq.forEach(item -> {
            stats.put(Long.valueOf(item.getUri().split("/events/")[1]), item);
        });

        List<EventFullDto> eventsDto = events.stream()
                .map(item -> mapperService.toEventDto(
                        item,
                        req.containsKey(item.getId()) ? req.get(item.getId()).size() : 0,
                        stats.containsKey(item.getId()) ? stats.get(item.getId()).getHits() : 0))
                .collect(Collectors.toList());

        return eventsDto;
    }

    private EventFullDto toEventDto(Event event) {
        List<Request> requests = requestRepository.findByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);

        List<ViewStats> stats = statsClient.getStats(LocalDateTime.of(2024, 1, 1, 0, 0, 0), LocalDateTime.now(), List.of("/events/" + event.getId()), true);
        if (stats == null) {
            throw new ServiceConnection("Возвращён пустой ответ сервера статистики");
        }

        return mapperService.toEventDto(event, requests.size(), stats.isEmpty() ? 0 : stats.get(0).getHits());
    }
}
