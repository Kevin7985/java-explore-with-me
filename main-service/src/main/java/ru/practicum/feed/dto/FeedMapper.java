package ru.practicum.feed.dto;

import org.springframework.stereotype.Component;
import ru.practicum.feed.model.Feed;
import ru.practicum.user.dto.UserDto;

@Component
public class FeedMapper {
    public FeedDto toFeedDto(Feed feed, UserDto userDto, Object entity) {
        return new FeedDto(
                feed.getId(),
                userDto,
                feed.getFeedType(),
                entity,
                feed.getCreated()
        );
    }
}
