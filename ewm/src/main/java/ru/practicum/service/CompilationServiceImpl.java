package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final RequestService requestService;
    private final StatsService statsService;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto dto) {
        log.info("Creating compilation with title: {}", dto.getTitle());

        List<Event> events = null;
        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            events = eventRepository.findAllById(dto.getEvents());
            if (events.size() != dto.getEvents().size()) {
                throw new NotFoundException("Some events not found");
            }
        }

        Compilation compilation = CompilationMapper.toEntity(dto, events != null ? events : List.of());
        Compilation saved = compilationRepository.save(compilation);

        Map<Long, Long> confirmedMap = requestService.getConfirmedRequestsMap(
                saved.getEvents().stream().map(Event::getId).collect(Collectors.toList())
        );
        Map<Long, Long> viewsMap = statsService.getViews(
                saved.getEvents().stream().map(Event::getId).collect(Collectors.toList())
        );

        log.info("Compilation created with id: {}", saved.getId());
        return CompilationMapper.toDto(saved, confirmedMap, viewsMap);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        log.info("Deleting compilation with id: {}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        compilationRepository.delete(compilation);
        log.info("Compilation with id: {} deleted", compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest dto) {
        log.info("Updating compilation with id: {}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        if (dto.getTitle() != null) {
            compilation.setTitle(dto.getTitle());
        }
        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }
        if (dto.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(dto.getEvents());
            if (events.size() != dto.getEvents().size()) {
                throw new NotFoundException("Some events not found");
            }
            compilation.setEvents(events.stream().collect(Collectors.toSet()));
        }

        Compilation updated = compilationRepository.save(compilation);

        Map<Long, Long> confirmedMap = requestService.getConfirmedRequestsMap(
                updated.getEvents().stream().map(Event::getId).collect(Collectors.toList())
        );
        Map<Long, Long> viewsMap = statsService.getViews(
                updated.getEvents().stream().map(Event::getId).collect(Collectors.toList())
        );

        return CompilationMapper.toDto(updated, confirmedMap, viewsMap);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        log.info("Getting compilations: pinned={}, from={}, size={}", pinned, from, size);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id"));

        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findByPinned(pinned, pageable).getContent();
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }

        List<Long> allEventIds = compilations.stream()
                .flatMap(c -> c.getEvents().stream())
                .map(Event::getId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Long> confirmedMap = requestService.getConfirmedRequestsMap(allEventIds);
        Map<Long, Long> viewsMap = statsService.getViews(allEventIds);

        return compilations.stream()
                .map(c -> CompilationMapper.toDto(c, confirmedMap, viewsMap))
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilation(Long compId) {
        log.info("Getting compilation with id: {}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        List<Long> eventIds = compilation.getEvents().stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        Map<Long, Long> confirmedMap = requestService.getConfirmedRequestsMap(eventIds);
        Map<Long, Long> viewsMap = statsService.getViews(eventIds);

        return CompilationMapper.toDto(compilation, confirmedMap, viewsMap);
    }
}