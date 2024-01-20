package ru.practicum.stats.dto;

import org.springframework.stereotype.Component;
import ru.practicum.dto.ViewStats;
import ru.practicum.stats.model.EndpointHit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class StatsMapper {
    public EndpointHit toHit(ru.practicum.dto.EndpointHit endpointHit) {
        return new EndpointHit(
                null,
                endpointHit.getApp(),
                endpointHit.getUri(),
                endpointHit.getIp(),
                LocalDateTime.parse(endpointHit.getTimestamp(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }

    public ViewStats toHitDto(EndpointHit endpointHit, Long hits) {
        return new ViewStats(
                endpointHit.getApp(),
                endpointHit.getUri(),
                hits
        );
    }
}
