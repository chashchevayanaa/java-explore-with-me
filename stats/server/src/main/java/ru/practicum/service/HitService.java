package ru.practicum.service;

import ru.practicum.dto.EndpointHitDto;

public interface HitService {
    void saveHit(EndpointHitDto endpointHitDto);
}