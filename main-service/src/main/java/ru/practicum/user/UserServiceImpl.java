package ru.practicum.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.feed.FeedRepository;
import ru.practicum.feed.model.Feed;
import ru.practicum.feed.model.FeedType;
import ru.practicum.service.MapperService;
import ru.practicum.service.ValidationService;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.exceptions.SubscriptionAlreadyExists;
import ru.practicum.user.exceptions.SubscriptionConflict;
import ru.practicum.user.exceptions.SubscriptionNotFound;
import ru.practicum.user.model.Subscription;
import ru.practicum.user.model.User;
import ru.practicum.utils.Pagination;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final MapperService mapperService;
    private final ValidationService validationService;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final FeedRepository feedRepository;

    @Override
    public UserDto createUser(NewUserRequest userRequest) {
        User newUser = mapperService.toUser(userRequest);

        log.info("Добавлен новый пользователь: " + newUser);
        return mapperService.toUserDto(userRepository.save(newUser));
    }

    @Override
    public List<UserDto> findUsers(List<Long> ids, Integer from, Integer size) {
        if (!ids.isEmpty()) {
            return userRepository.findByIdIn(ids).stream()
                    .map(mapperService::toUserDto)
                    .collect(Collectors.toList());
        } else {
            List<UserDto> users = new ArrayList<>();

            Pageable pageable;
            Sort sort = Sort.by(Sort.Direction.ASC, "id");
            Page<User> page;
            Pagination pager = new Pagination(from, size);

            if (size == null) {
                pageable = PageRequest.of(pager.getPageStart(), pager.getPageSize(), sort);
                page = userRepository.findAll(pageable);

                while (page.hasContent()) {
                    users.addAll(page.stream()
                            .map(mapperService::toUserDto)
                            .collect(Collectors.toList()));
                    pageable = pageable.next();
                    page = userRepository.findAll(pageable);
                }
            } else {
                for (int i = pager.getPageStart(); i < pager.getPagesAmount(); i++) {
                    pageable = PageRequest.of(i, pager.getPageSize(), sort);
                    page = userRepository.findAll(pageable);
                    users.addAll(page.stream()
                            .map(mapperService::toUserDto)
                            .collect(Collectors.toList()));
                }

                users = users.stream().limit(size).collect(Collectors.toList());
            }

            log.info("Получен список всех пользователей");
            return users;
        }
    }

    @Override
    public void deleteUser(Long userId) {
        validationService.validateUser(userId);

        log.info("Удалён пользователь c id = " + userId);
        userRepository.deleteById(userId);
    }

    @Override
    public void subscribe(Long fromUserId, Long toUserId) {
        if (Objects.equals(fromUserId, toUserId)) {
            throw new SubscriptionConflict("Невозможно подписаться на самого себя");
        }

        User fromUser = validationService.validateUser(fromUserId);
        validationService.validateUser(toUserId);

        Optional<Subscription> found = subscriptionRepository.findByFromUserIdAndToUserId(fromUserId, toUserId);
        if (found.isPresent()) {
            throw new SubscriptionAlreadyExists("Подписка уже оформлена");
        }

        Subscription subscription = new Subscription(
                null,
                fromUserId,
                toUserId
        );

        log.info("Оформлена подписка: " + fromUserId + " -> " + toUserId);
        subscriptionRepository.save(subscription);

        feedRepository.save(new Feed(
                null,
                fromUser,
                FeedType.SUBSCRIBE,
                toUserId,
                LocalDateTime.now()
        ));
    }

    @Override
    public void unsubscribe(Long fromUserId, Long toUserId) {
        validationService.validateUser(fromUserId);
        validationService.validateUser(toUserId);

        Optional<Subscription> found = subscriptionRepository.findByFromUserIdAndToUserId(fromUserId, toUserId);
        if (found.isEmpty()) {
            throw new SubscriptionNotFound("Подписка не найдена");
        }

        log.info("Отменена подписка: " + fromUserId + " -> " + toUserId);
        subscriptionRepository.deleteById(found.get().getId());

        Optional<Feed> foundFeed = feedRepository.findByUser_IdAndFeedTypeAndEntityId(fromUserId, FeedType.SUBSCRIBE, toUserId);
        foundFeed.ifPresent(feed -> feedRepository.deleteById(feed.getId()));
    }

    @Override
    public List<UserShortDto> getUserSubscribers(Long id) {
        User user = validationService.validateUser(id);

        List<Long> subscriberIds = subscriptionRepository.findByToUserId(id).stream().map(Subscription::getFromUserId).collect(Collectors.toList());
        List<User> users = userRepository.findByIdIn(subscriberIds);

        log.info("Получен список подписчиков пользователя");
        return users.stream()
                .map(mapperService::toUserShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserShortDto> getUserSubscriptions(Long id) {
        User user = validationService.validateUser(id);

        List<Long> subscritionIds = subscriptionRepository.findByFromUserId(id).stream().map(Subscription::getFromUserId).collect(Collectors.toList());
        List<User> users = userRepository.findByIdIn(subscritionIds);

        log.info("Получен список подписок пользователя");
        return users.stream()
                .map(mapperService::toUserShortDto)
                .collect(Collectors.toList());
    }
}
