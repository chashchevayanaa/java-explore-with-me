package ru.practicum.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.service.HitService;

@RestController
public class HitController {

    private final HitService hitService;

    public HitController(HitService hitService) {
        this.hitService = hitService;
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveHit(@RequestBody EndpointHitDto endpointHitDto) {
        hitService.saveHit(endpointHitDto);

    }
}
