package ru.practicum.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.practicum.event.model.Event;

import java.util.List;

public interface EventRepository extends PagingAndSortingRepository<Event, Long>, QuerydslPredicateExecutor<Event> {
    Page<Event> findByInitiator_Id(Long userId, Pageable page);

    List<Event> findByIdIn(List<Long> ids);
}
