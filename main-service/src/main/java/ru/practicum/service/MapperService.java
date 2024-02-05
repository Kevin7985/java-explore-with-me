package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.CategoryMapper;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.CompilationMapper;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventMapper;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;
import ru.practicum.feed.dto.FeedDto;
import ru.practicum.feed.dto.FeedMapper;
import ru.practicum.feed.model.Feed;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserMapper;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MapperService {
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final EventMapper eventMapper;
    private final RequestMapper requestMapper;
    private final CompilationMapper compilationMapper;
    private final FeedMapper feedMapper;

    public User toUser(NewUserRequest userRequest) {
        return userMapper.toUser(userRequest);
    }

    public UserDto toUserDto(User user) {
        return userMapper.toUserDto(user);
    }

    public UserShortDto toUserShortDto(User user) {
        return userMapper.toUserShortDto(user);
    }

    public Category toCategory(NewCategoryDto categoryDto) {
        return categoryMapper.toCategory(categoryDto);
    }

    public CategoryDto toCategoryDto(Category category) {
        return categoryMapper.toCategoryDto(category);
    }

    public Event toEvent(NewEventDto eventDto, Category category, User initiator) {
        return eventMapper.toEvent(eventDto, category, initiator);
    }

    public EventFullDto toEventDto(Event event, Integer confirmedRequests, Long views) {
        return eventMapper.toEventDto(
                event,
                toCategoryDto(event.getCategory()),
                toUserShortDto(event.getInitiator()),
                confirmedRequests,
                views
        );
    }

    public EventShortDto toEventShortDto(Event event, Integer confirmedRequests, Long views) {
        return eventMapper.toEventShotDto(
                event,
                toCategoryDto(event.getCategory()),
                toUserShortDto(event.getInitiator()),
                confirmedRequests,
                views
        );
    }

    public ParticipationRequestDto toRequestDto(Request request) {
        return requestMapper.toRequestDto(request);
    }

    public Compilation toCompilation(NewCompilationDto compilationDto, List<Event> events) {
        return compilationMapper.toCompilation(compilationDto, events);
    }

    public CompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> events) {
        return compilationMapper.toCompilationDto(compilation, events);
    }

    public FeedDto toFeedDto(Feed feed, UserDto userDto, Object entity) {
        return feedMapper.toFeedDto(feed, userDto, entity);
    }
}
