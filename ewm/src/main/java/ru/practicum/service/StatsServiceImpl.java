package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private static final LocalDateTime DEFAULT_START = LocalDateTime.of(2000, 1, 1, 0, 0);
    private final StatsClient statsClient;

    @Override
    public void saveHit(String uri, String ip) {
        log.info("Saving hit: uri={}, ip={}", uri, ip);

        EndpointHitDto hit = new EndpointHitDto();
        hit.setApp("ewm-main-service");
        hit.setUri(uri);
        hit.setIp(ip);
        hit.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        statsClient.saveHit(hit);
    }

    @Override
    public long getViews(Long eventId) {
        return getViews(List.of(eventId)).getOrDefault(eventId, 0L);
    }

    @Override
    public Map<Long, Long> getViews(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());

        List<ViewStatsDto> stats = statsClient.getStats(DEFAULT_START, LocalDateTime.now().plusYears(1), uris, true);

        if (stats == null || stats.isEmpty()) {
            return Map.of();
        }

        return stats.stream()
                .collect(Collectors.toMap(
                        dto -> Long.parseLong(dto.getUri().replace("/events/", "")),
                        ViewStatsDto::getHits
                ));
    }
}