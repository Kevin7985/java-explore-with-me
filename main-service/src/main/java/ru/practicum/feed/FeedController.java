package ru.practicum.feed;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.feed.dto.FeedDto;
import ru.practicum.feed.model.FeedType;

import javax.validation.constraints.Min;
import java.util.List;

@RestController
@AllArgsConstructor
public class FeedController {
    private final FeedService feedService;

    @GetMapping("/users/{id}/feed")
    public List<FeedDto> getFeed(
            @PathVariable Long id,
            @RequestParam(defaultValue = "ALL") FeedType feedType,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        return feedService.getUserFeed(id, feedType, from, size);
    }
}
