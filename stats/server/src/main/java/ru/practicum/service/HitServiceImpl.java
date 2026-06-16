package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.repository.StatsRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class HitServiceImpl implements HitService {

    private final StatsRepository statsRepository;

    @Override
    public void saveHit(EndpointHitDto endpointHitDto) {
        log.debug("Saving hit to database: {}", endpointHitDto);
        statsRepository.save(EndpointHitMapper.toEntity(endpointHitDto));
        log.debug("Hit saved successfully");
    }
}