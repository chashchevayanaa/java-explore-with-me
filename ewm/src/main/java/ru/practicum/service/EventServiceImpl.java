package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.*;
import ru.practicum.exception.BadRequestException;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hibernate.validator.internal.engine.messageinterpolation.el.RootResolver.FORMATTER;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final StatsService statsService;
    private static RequestService requestService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
        return EventMapper.toEventShortDtoList(events, Map.of(), Map.of());
    }

    @Override
    public EventFullDto getEventById(Long userId, Long eventId) {
        log.info("Getting event id: {} for user id: {}", eventId, userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
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
                    throw new BadRequestException("Unknown state action: " + dto.getStateAction());
            }
        }

        Event updated = eventRepository.save(event);

        log.info("Event updated with id: {}", updated.getId());
        return EventMapper.toEventFullDto(updated);
    }

    @Override
    public List<EventFullDto> getAdminEvents(List<Long> users, List<String> states, List<Long> categories,
                                             String rangeStart, String rangeEnd, int from, int size) {
        log.info("Admin search events: users={}, states={}, categories={}, from={}, size={}",
                users, states, categories, from, size);

        Specification<Event> spec = Specification.where(null);

        if (users != null && !users.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("initiator").get("id").in(users));
        }

        if (states != null && !states.isEmpty()) {
            List<EventState> eventStates = states.stream()
                    .map(EventState::valueOf)
                    .collect(Collectors.toList());
            spec = spec.and((root, query, cb) -> root.get("state").in(eventStates));
        }

        if (categories != null && !categories.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("category").get("id").in(categories));
        }

        if (rangeStart != null && rangeEnd != null) {
            LocalDateTime start = LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime end = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            spec = spec.and((root, query, cb) -> cb.between(root.get("eventDate"), start, end));
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id"));
        Page<Event> page = eventRepository.findAll(spec, pageable);

        List<Long> eventIds = page.getContent().stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> confirmedMap = requestService.getConfirmedRequestsMap(eventIds);
        Map<Long, Long> viewsMap = statsService.getViews(eventIds);

        return EventMapper.toEventFullDtoList(page.getContent(), confirmedMap, viewsMap);
    }

    @Override
    @Transactional
    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest dto) {
        log.info("Admin update event id: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

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
            if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ConflictException("Event date must be at least 1 hour from now");
            }
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
                case "PUBLISH_EVENT":
                    if (event.getState() != EventState.PENDING) {
                        throw new ConflictException("Only pending events can be published");
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case "REJECT_EVENT":
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new ConflictException("Published events cannot be rejected");
                    }
                    event.setState(EventState.CANCELED);
                    break;
                default:
                    throw new BadRequestException("Unknown state action: " + dto.getStateAction());
            }
        }

        Event updated = eventRepository.save(event);

        Map<Long, Long> confirmedMap = requestService.getConfirmedRequestsMap(List.of(updated.getId()));
        Map<Long, Long> viewsMap = statsService.getViews(List.of(updated.getId()));

        return EventMapper.toEventFullDto(
                updated,
                confirmedMap.getOrDefault(updated.getId(), 0L),
                viewsMap.getOrDefault(updated.getId(), 0L)
        );
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               String rangeStart, String rangeEnd, boolean onlyAvailable,
                                               String sort, int from, int size) {
        log.info("Public search events: text={}, categories={}, paid={}, onlyAvailable={}, sort={}",
                text, categories, paid, onlyAvailable, sort);
        Specification<Event> spec = Specification.where(null);
        spec = spec.and((root, query, cb) -> cb.equal(root.get("state"), EventState.PUBLISHED));

        if (text != null && !text.isBlank()) {
            String likePattern = "%" + text.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("annotation")), likePattern),
                    cb.like(cb.lower(root.get("description")), likePattern)
            ));
        }

        if (categories != null && !categories.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("category").get("id").in(categories));
        }
        if (paid != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("paid"), paid));
        }
        if (rangeStart != null && rangeEnd != null) {
            LocalDateTime start = LocalDateTime.parse(rangeStart, FORMATTER);
            LocalDateTime end = LocalDateTime.parse(rangeEnd, FORMATTER);
            spec = spec.and((root, query, cb) -> cb.between(root.get("eventDate"), start, end));
        } else {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThan(root.get("eventDate"), LocalDateTime.now())
            );
        }

        if (onlyAvailable) {
            spec = spec.and((root, query, cb) -> {
                return cb.or(
                        cb.equal(root.get("participantLimit"), 0),
                        cb.lessThan(
                                cb.size(root.get("requests")),  // нужно добавить поле requests в Event
                                root.get("participantLimit")
                        )
                );
            });
        }
        Sort sortOrder;
        if (sort != null && sort.equals("EVENT_DATE")) {
            sortOrder = Sort.by("eventDate").ascending();
        } else if (sort != null && sort.equals("VIEWS")) {
            sortOrder = Sort.by("id").ascending(); // временно
        } else {
            sortOrder = Sort.by("id").ascending();
        }

        Pageable pageable = PageRequest.of(from / size, size, sortOrder);
        Page<Event> page = eventRepository.findAll(spec, pageable);

        List<Long> eventIds = page.getContent().stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> confirmedMap = requestService.getConfirmedRequestsMap(eventIds);
        Map<Long, Long> viewsMap = statsService.getViews(eventIds);

        List<Event> events = page.getContent();
        if (sort != null && sort.equals("VIEWS")) {
            events = events.stream()
                    .sorted((e1, e2) -> Long.compare(
                            viewsMap.getOrDefault(e2.getId(), 0L),
                            viewsMap.getOrDefault(e1.getId(), 0L)
                    ))
                    .collect(Collectors.toList());
        }

        return EventMapper.toEventShortDtoList(events, confirmedMap, viewsMap);
    }


    public EventFullDto getPublicEvent(Long eventId) {
        log.info("Get public event id: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
        Map<Long, Long> confirmedMap = requestService.getConfirmedRequestsMap(List.of(eventId));
        Map<Long, Long> viewsMap = statsService.getViews(List.of(eventId));

        return EventMapper.toEventFullDto(
                event,
                confirmedMap.getOrDefault(eventId, 0L),
                viewsMap.getOrDefault(eventId, 0L)
        );
    }

}