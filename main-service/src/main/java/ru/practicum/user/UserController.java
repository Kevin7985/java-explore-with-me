package ru.practicum.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/admin/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody @Valid NewUserRequest userRequest) {
        return userService.createUser(userRequest);
    }

    @GetMapping("/admin/users")
    public List<UserDto> findUsers(
            @RequestParam(required = false, defaultValue = "") List<Long> ids,
            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
            @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size
    ) {
        return userService.findUsers(ids, from, size);
    }

    @DeleteMapping("/admin/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }

    @PostMapping("/users/{fromUserId}/subscribe")
    @ResponseStatus(HttpStatus.CREATED)
    public void subscribe(@PathVariable Long fromUserId, @RequestParam(required = true) Long toUserId) {
        userService.subscribe(fromUserId, toUserId);
    }

    @DeleteMapping("/users/{fromUserId}/subscribe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@PathVariable Long fromUserId, @RequestParam(required = true) Long toUserId) {
        userService.unsubscribe(fromUserId, toUserId);
    }

    @GetMapping("/users/{id}/subscribers")
    public List<UserShortDto> getUserSubscribers(@PathVariable Long id) {
        return userService.getUserSubscribers(id);
    }

    @GetMapping("/users/{id}/subscriptions")
    public List<UserShortDto> getUserSubscriptions(@PathVariable Long id) {
        return userService.getUserSubscriptions(id);
    }
}
