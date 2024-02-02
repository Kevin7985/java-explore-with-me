package ru.practicum.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.service.MapperService;
import ru.practicum.service.ValidationService;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.User;
import ru.practicum.utils.Pagination;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final MapperService mapperService;
    private final ValidationService validationService;
    private final UserRepository userRepository;

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
}
