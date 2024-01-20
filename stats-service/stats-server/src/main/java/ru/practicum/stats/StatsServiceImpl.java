package ru.practicum.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.stats.dto.StatsMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository repository;
    private final StatsMapper mapper;

    public ViewStats addStats(EndpointHit endpointHit) {
        repository.save(mapper.toHit(endpointHit));
        return null;
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, java.util.List<String> uris, Boolean unique) {
        if (uris == null) {
            return unique ? repository.findWithoutUriUnique(start, end) : repository.findWithoutUriNotUnique(start, end);
        } else {
            return unique ? repository.findWithUriUnique(start, end, uris) : repository.findWithUriNotUnique(start, end, uris);
        }
    }

}
