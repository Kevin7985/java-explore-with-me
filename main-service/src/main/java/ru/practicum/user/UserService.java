package ru.practicum.user;

import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;

import java.util.List;

public interface UserService {
    UserDto createUser(NewUserRequest userRequest);

    List<UserDto> findUsers(List<Long> ids, Integer from, Integer size);

    void deleteUser(Long userId);

    void subscribe(Long fromUserId, Long toUserId);

    void unsubscribe(Long fromUserId, Long toUserId);

    List<UserShortDto> getUserSubscribers(Long id);

    List<UserShortDto> getUserSubscriptions(Long id);
}
