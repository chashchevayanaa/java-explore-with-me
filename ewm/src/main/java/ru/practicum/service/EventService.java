package ru.practicum.service;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;

import java.util.List;

public interface EventService {
    EventFullDto createEvent(Long userId, NewEventDto dto);

    List<EventShortDto> getUserEvents(Long userId, int from, int size);

    EventFullDto getEventById(Long userId, Long eventId);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest dto);

}
