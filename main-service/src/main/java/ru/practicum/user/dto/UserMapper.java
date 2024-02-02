package ru.practicum.user.dto;

import org.springframework.stereotype.Component;
import ru.practicum.user.model.User;
import ru.practicum.user.model.UserRole;

@Component
public class UserMapper {
    public User toUser(NewUserRequest userRequest) {
        return new User(
                null,
                userRequest.getEmail(),
                userRequest.getName(),
                UserRole.USER
        );
    }

    public UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getName()
        );
    }

    public UserShortDto toUserShortDto(User user) {
        return new UserShortDto(
                user.getId(),
                user.getName()
        );
    }
}
