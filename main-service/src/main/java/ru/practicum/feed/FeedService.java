package ru.practicum.feed;

import ru.practicum.feed.dto.FeedDto;
import ru.practicum.feed.model.FeedType;

import java.util.List;

public interface FeedService {
    List<FeedDto> getUserFeed(Long userId, FeedType feedType, Integer from, Integer size);
}
