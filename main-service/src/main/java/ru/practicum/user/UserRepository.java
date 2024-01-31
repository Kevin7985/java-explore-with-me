package ru.practicum.user;

import org.springframework.data.repository.PagingAndSortingRepository;
import ru.practicum.user.model.User;

import java.util.List;

public interface UserRepository extends PagingAndSortingRepository<User, Long> {
    List<User> findByIdIn(List<Long> ids);
}
