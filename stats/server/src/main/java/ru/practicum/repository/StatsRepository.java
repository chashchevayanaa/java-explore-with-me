package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    // статистика ВСЕХ посещений без уникальности
    @Query("SELECT new ru.practicum.model.ViewStats(h.app, h.uri, COUNT(h.uri)) " +
            "FROM EndpointHit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri")
    List<ViewStats> findStats(@Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end);

    // статистика по всем сайтам уникальных людей
    @Query("SELECT new ru.practicum.model.ViewStats(h.app, h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM EndpointHit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri")
    List<ViewStats> findStatsUnique(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);

    // по конкретному сайту без уникальности
    @Query("SELECT new ru.practicum.model.ViewStats(h.app, h.uri, COUNT(h.uri)) " +
            "FROM EndpointHit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND h.uri IN :uri " +
            "GROUP BY h.app, h.uri")
    List<ViewStats> findStatsWithUris(@Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end,
                                      @Param("uri") List<String> uri);

    // по конкретному сайту с уникальностью
    @Query("SELECT new ru.practicum.model.ViewStats(h.app, h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM EndpointHit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND h.uri IN :uri " +
            "GROUP BY h.app, h.uri")
    List<ViewStats> findStatsUniqueWithUris(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end,
                                            @Param("uri") List<String> uri);
}