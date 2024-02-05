package ru.practicum.feed;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.practicum.feed.model.Feed;
import ru.practicum.feed.model.FeedType;

import java.util.List;
import java.util.Optional;

public interface FeedRepository extends PagingAndSortingRepository<Feed, Long> {
    Optional<Feed> findByUser_IdAndFeedTypeAndEntityId(Long userId, FeedType feedType, Long entityId);

    Page<Feed> findByUser_IdIn(List<Long> subscriptionIds, Pageable pageable);

    Page<Feed> findByUser_IdInAndFeedType(List<Long> subscriptionIds, FeedType feedType, Pageable pageable);
}
