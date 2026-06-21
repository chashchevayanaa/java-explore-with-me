package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.LocationMapper;
import ru.practicum.model.*;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.LocationRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        log.info("Creating event for user id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id=" + dto.getCategory() + " was not found"));
        // Проверка: событие должно быть минимум через 2 часа
        if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Event date must be at least 2 hours from now");
        }

        Location location = LocationMapper.toLocation(dto.getLocation());
        location = locationRepository.save(location);

        Event event = EventMapper.toEvent(dto, user, category, location);
        event = eventRepository.save(event);

        log.info("Event created with id: {}", event.getId());
        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        log.info("Getting events for user id: {}, from={}, size={}", userId, from, size);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));

        List<Event> events = eventRepository.findByInitiatorId(userId, pageable).getContent();

        log.info("Found {} events for user id: {}", events.size(), userId);
        return EventMapper.toEventShortDtoList(events);
    }

    @Override
    public EventFullDto getEventById(Long userId, Long eventId) {
        log.info("Getting event id: {} for user id: {}", eventId, userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        return EventMapper.toEventFullDto(event);
    }

    @Override
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest dto) {
        log.info("Updating event id: {} for user id: {}", eventId, userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        // проверка: можно редактировать только PENDING или CANCELED
        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        // Проверка: если меняется дата, она должна быть минимум через 2 часа
        if (dto.getEventDate() != null && dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Event date must be at least 2 hours from now");
        }

        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }

        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + dto.getCategory() + " was not found"));
            event.setCategory(category);
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getEventDate() != null) {
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getLocation() != null) {
            Location location = LocationMapper.toLocation(dto.getLocation());
            location = locationRepository.save(location);
            event.setLocation(location);
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }

        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case "CANCEL":
                    event.setState(EventState.CANCELED);
                    break;
                case "SEND_TO_REVIEW":
                    event.setState(EventState.PENDING);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown state action: " + dto.getStateAction());
            }
        }

        Event updated = eventRepository.save(event);

        log.info("Event updated with id: {}", updated.getId());
        return EventMapper.toEventFullDto(updated);
    }
}


