package ru.practicum.mapper;

import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CompilationMapper {

    public static Compilation toEntity(NewCompilationDto dto, List<Event> events) {
        Compilation compilation = new Compilation();
        compilation.setPinned(dto.getPinned() != null ? dto.getPinned() : false);
        compilation.setTitle(dto.getTitle());
        compilation.setEvents(events.stream().collect(Collectors.toSet()));
        return compilation;
    }

    public static CompilationDto toDto(Compilation compilation,
                                       Map<Long, Long> confirmedMap,
                                       Map<Long, Long> viewsMap,
                                       boolean onlyPublished) {
        CompilationDto dto = new CompilationDto();
        dto.setId(compilation.getId());
        dto.setPinned(compilation.getPinned());
        dto.setTitle(compilation.getTitle());

        if (compilation.getEvents() != null) {
            List<Event> events = compilation.getEvents().stream()
                    .filter(e -> !onlyPublished || e.getState() == EventState.PUBLISHED)
                    .collect(Collectors.toList());
            dto.setEvents(EventMapper.toEventShortDtoList(events, confirmedMap, viewsMap));
        }

        return dto;
    }
}