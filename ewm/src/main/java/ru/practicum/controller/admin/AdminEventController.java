package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.service.EventService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/events")
public class AdminEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /admin/events - users={}, states={}, categories={}, from={}, size={}",
                users, states, categories, from, size);
        return eventService.getAdminEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateAdminEvent(@PathVariable Long eventId, @Valid @RequestBody UpdateEventAdminRequest dto) {
        log.info("PATCH /admin/events/{}", eventId);
        return eventService.updateAdminEvent(eventId, dto);
    }
}