package ru.practicum.mapper;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventMapper {

    public static Event toEvent(NewEventDto dto, User initiator, Category category, Location location) {
        Event event = new Event();
        event.setAnnotation(dto.getAnnotation());
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());
        event.setDescription(dto.getDescription());
        event.setEventDate(dto.getEventDate());
        event.setInitiator(initiator);
        event.setLocation(location);
        event.setPaid(dto.getPaid() != null ? dto.getPaid() : false);
        event.setParticipantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0);
        event.setRequestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true);
        event.setTitle(dto.getTitle());
        event.setState(ru.practicum.model.EventState.PENDING);
        return event;
    }

    // Базовые методы (без счётчиков) — для Private API
    public static EventFullDto toEventFullDto(Event event) {
        EventFullDto dto = new EventFullDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(CategoryMapper.toCategoryDto(event.getCategory()));
        dto.setConfirmedRequests(0L);
        dto.setCreatedOn(event.getCreatedOn());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());
        dto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
        dto.setLocation(LocationMapper.toLocationDto(event.getLocation()));
        dto.setPaid(event.getPaid());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setPublishedOn(event.getPublishedOn());
        dto.setRequestModeration(event.getRequestModeration());
        dto.setState(event.getState());
        dto.setTitle(event.getTitle());
        dto.setViews(0L);
        return dto;
    }

    public static EventShortDto toEventShortDto(Event event) {
        EventShortDto dto = new EventShortDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(CategoryMapper.toCategoryDto(event.getCategory()));
        dto.setConfirmedRequests(0L);
        dto.setEventDate(event.getEventDate());
        dto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
        dto.setPaid(event.getPaid());
        dto.setTitle(event.getTitle());
        dto.setViews(0L);
        return dto;
    }

    // Перегрузки со счётчиками
    public static EventFullDto toEventFullDto(Event event, long confirmedRequests, long views) {
        EventFullDto dto = toEventFullDto(event);
        dto.setConfirmedRequests(confirmedRequests);
        dto.setViews(views);
        return dto;
    }

    public static EventShortDto toEventShortDto(Event event, long confirmedRequests, long views) {
        EventShortDto dto = toEventShortDto(event);
        dto.setConfirmedRequests(confirmedRequests);
        dto.setViews(views);
        return dto;
    }

    public static List<EventShortDto> toEventShortDtoList(List<Event> events,
                                                          Map<Long, Long> confirmedMap,
                                                          Map<Long, Long> viewsMap) {
        return events.stream()
                .map(event -> toEventShortDto(
                        event,
                        confirmedMap.getOrDefault(event.getId(), 0L),
                        viewsMap.getOrDefault(event.getId(), 0L)
                ))
                .collect(Collectors.toList());
    }

    public static List<EventFullDto> toEventFullDtoList(List<Event> events,
                                                        Map<Long, Long> confirmedMap,
                                                        Map<Long, Long> viewsMap) {
        return events.stream()
                .map(event -> toEventFullDto(
                        event,
                        confirmedMap.getOrDefault(event.getId(), 0L),
                        viewsMap.getOrDefault(event.getId(), 0L)
                ))
                .collect(Collectors.toList());
    }
}