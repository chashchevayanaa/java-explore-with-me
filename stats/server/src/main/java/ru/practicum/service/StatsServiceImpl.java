package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.model.ViewStats;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (uris == null || uris.isEmpty()) {
            if (unique) {
                return statsRepository.findStatsUnique(start, end);
            } else {
                return statsRepository.findStats(start, end);
            }
        } else {
            if (unique) {
                return statsRepository.findStatsUniqueWithUris(start, end, uris);
            } else {
                return statsRepository.findStatsWithUris(start, end, uris);
            }
        }
    }
}
