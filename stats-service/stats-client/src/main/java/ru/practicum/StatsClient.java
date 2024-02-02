package ru.practicum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.client.BaseClient;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsClient extends BaseClient {

    private final String url;

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build()
        );

        url = serverUrl;
    }

    public ResponseEntity saveEndpoint(EndpointHit endpointHit) {
        return post("/hit", endpointHit);
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Не заданы даты поиска");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> params = new HashMap<>();
        params.put("start", URLEncoder.encode(start.format(formatter), StandardCharsets.UTF_8));
        params.put("end", URLEncoder.encode(end.format(formatter), StandardCharsets.UTF_8));
        if (unique != null) {
            params.put("unique", unique);
        }

        List<ViewStats> stats = Arrays.asList(rest.getForEntity(this.url + "/stats?start={start}&end={end}&uris=" + String.join("&uris=", uris) + "&unique={unique}", ViewStats[].class, params).getBody());

        return stats;
    }
}
