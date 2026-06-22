package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.StatsClient;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final LocalDateTime DEFAULT_START = LocalDateTime.of(2000, 1, 1, 0, 0);
    private final StatsClient statsClient;

    @Override
    public void saveHit(String uri, String ip) {
    }

    @Override
    public long getViews(Long eventIds) {
        return 0L;
    }

    @Override
    public Map<Long, Long> getViews(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());

        LocalDateTime start = LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.now().plusYears(1);

        List<ViewStatsDto> stats = statsClient.getStats(start, end, uris, true);

        return stats.stream()
                .collect(Collectors.toMap(
                        dto -> Long.parseLong(dto.getUri().replace("/events/", "")),
                        ViewStatsDto::getHits
                ));
    }
}