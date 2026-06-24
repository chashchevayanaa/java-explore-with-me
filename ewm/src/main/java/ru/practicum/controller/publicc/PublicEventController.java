package ru.practicum.controller.publicc;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.service.EventService;
import ru.practicum.service.StatsService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final EventService eventService;
    private final StatsService statsService;

    @GetMapping
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(defaultValue = "false") boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        log.info("GET /events - text={}, categories={}, paid={}, onlyAvailable={}, sort={}, from={}, size={}",
                text, categories, paid, onlyAvailable, sort, from, size);

        try {
            statsService.saveHit(request.getRequestURI(), request.getRemoteAddr());
        } catch (Exception e) {
            log.warn("Failed to save hit for uri {}: {}", request.getRequestURI(), e.getMessage());
        }

        return eventService.getPublicEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
    }

    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        log.info("GET /events/{}", id);

        statsService.saveHit("/events/" + id, request.getRemoteAddr());

        return eventService.getPublicEvent(id);
    }
}