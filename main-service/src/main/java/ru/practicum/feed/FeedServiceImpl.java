package ru.practicum.feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.event.EventRepository;
import ru.practicum.event.EventService;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.Event;
import ru.practicum.feed.dto.FeedDto;
import ru.practicum.feed.model.Feed;
import ru.practicum.feed.model.FeedType;
import ru.practicum.service.MapperService;
import ru.practicum.service.ValidationService;
import ru.practicum.user.SubscriptionRepository;
import ru.practicum.user.UserRepository;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.Subscription;
import ru.practicum.utils.Pagination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedServiceImpl implements FeedService {
    private final MapperService mapperService;
    private final ValidationService validationService;
    private final EventService eventService;
    private final SubscriptionRepository subscriptionRepository;
    private final FeedRepository feedRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public List<FeedDto> getUserFeed(Long userId, FeedType feedType, Integer from, Integer size) {
        validationService.validateUser(userId);

        List<Long> subscriptions = subscriptionRepository.findByFromUserId(userId).stream()
                .map(Subscription::getToUserId)
                .collect(Collectors.toList());

        Pageable pageable;
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Page<Feed> page;
        Pagination pager = new Pagination(from, size);

        List<Feed> tempFeed = new ArrayList<>();

        if (size == null) {
            pageable = PageRequest.of(pager.getPageStart(), pager.getPageSize(), sort);
            page = feedType.equals(FeedType.ALL) ?
                    feedRepository.findByUser_IdIn(subscriptions, pageable) :
                    feedRepository.findByUser_IdInAndFeedType(subscriptions, feedType, pageable);

            while (page.hasContent()) {
                tempFeed.addAll(page.toList());
                pageable = pageable.next();
                page = feedType.equals(FeedType.ALL) ?
                        feedRepository.findByUser_IdIn(subscriptions, pageable) :
                        feedRepository.findByUser_IdInAndFeedType(subscriptions, feedType, pageable);
            }
        } else {
            for (int i = pager.getPageStart(); i < pager.getPagesAmount(); i++) {
                pageable = PageRequest.of(i, pager.getPageSize(), sort);
                page = feedType.equals(FeedType.ALL) ?
                        feedRepository.findByUser_IdIn(subscriptions, pageable) :
                        feedRepository.findByUser_IdInAndFeedType(subscriptions, feedType, pageable);
                tempFeed.addAll(page.toList());
            }

            tempFeed = tempFeed.stream().limit(size).collect(Collectors.toList());
        }

        return toFeedDto(tempFeed);
    }

    private List<FeedDto> toFeedDto(List<Feed> feed) {
        List<Long> userIds = new ArrayList<>();
        List<Long> eventIds = new ArrayList<>();

        feed.forEach(item -> {
            if (item.getFeedType().equals(FeedType.SUBSCRIBE)) {
                userIds.add(item.getEntityId());
            } else if (item.getFeedType().equals(FeedType.PARTICIPATE) || item.getFeedType().equals(FeedType.CREATE_NEW_EVENT)) {
                eventIds.add(item.getEntityId());
            }
        });

        List<UserShortDto> users = userIds.isEmpty() ? new ArrayList<>() : userRepository.findByIdIn(userIds).stream().map(mapperService::toUserShortDto).collect(Collectors.toList());
        HashMap<Long, UserShortDto> usersMap = new HashMap<>();
        users.forEach(item -> usersMap.put(item.getId(), item));

        List<Event> events = eventIds.isEmpty() ? new ArrayList<>() : eventRepository.findByIdIn(eventIds);

        List<EventFullDto> eventFullDtoList = eventService.toEventDto(events);
        HashMap<Long, EventFullDto> eventsFullDtoMap = new HashMap<>();
        eventFullDtoList.forEach(item -> eventsFullDtoMap.put(item.getId(), item));

        HashMap<Long, EventShortDto> eventsDto = new HashMap<>();
        events.forEach(item -> {
            EventShortDto eventShortDto = mapperService.toEventShortDto(
                    item,
                    eventsFullDtoMap.get(item.getId()).getConfirmedRequests(),
                    eventsFullDtoMap.get(item.getId()).getViews()
            );

            eventsDto.put(item.getId(), eventShortDto);
        });

        return feed.stream()
                .map(item -> {
                    if (item.getFeedType().equals(FeedType.SUBSCRIBE)) {
                        return mapperService.toFeedDto(item, mapperService.toUserDto(item.getUser()), usersMap.get(item.getEntityId()));
                    } else {
                        return mapperService.toFeedDto(item, mapperService.toUserDto(item.getUser()), eventsDto.get(item.getEntityId()));
                    }
                }).collect(Collectors.toList());
    }
}
