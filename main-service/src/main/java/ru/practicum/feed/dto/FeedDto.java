package ru.practicum.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.feed.model.FeedType;
import ru.practicum.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class FeedDto {
    private final Long id;
    private final UserDto user;
    private final FeedType feedType;
    private final Object entity;
    private final LocalDateTime created;
}
