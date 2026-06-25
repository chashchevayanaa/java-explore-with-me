package ru.practicum.service;

import java.util.List;
import java.util.Map;

public interface StatsService {
    void saveHit(String uri, String ip);

    long getViews(Long eventId);

    Map<Long, Long> getViews(List<Long> eventIds);
}
