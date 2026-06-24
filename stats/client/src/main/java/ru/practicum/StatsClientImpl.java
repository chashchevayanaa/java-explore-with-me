package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class StatsClientImpl implements StatsClient {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public StatsClientImpl(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public void saveHit(EndpointHitDto hitDto) {
        String url = baseUrl + "/hit";
        log.debug("Saving hit to URL: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<EndpointHitDto> requestEntity = new HttpEntity<>(hitDto, headers);

        restTemplate.exchange(url, HttpMethod.POST, requestEntity, Void.class);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/stats")
                .queryParam("start", start.format(FORMATTER))
                .queryParam("end", end.format(FORMATTER))
                .queryParam("unique", unique);

        if (start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date must be before end date");
        }

        if (uris != null && !uris.isEmpty()) {
            uris.forEach(u -> builder.queryParam("uris", u));
        }

        URI uri = builder.build().encode().toUri();
        log.info("Stats request URI: {}", uri);

        ResponseEntity<List<ViewStatsDto>> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ViewStatsDto>>() {
                }
        );

        return response.getBody();
    }
}