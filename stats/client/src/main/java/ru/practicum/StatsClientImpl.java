package ru.practicum;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<EndpointHitDto> requestEntity = new HttpEntity<>(hitDto, headers);

        restTemplate.exchange(url, HttpMethod.POST, requestEntity, Void.class);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        StringBuilder urlBuilder = new StringBuilder(baseUrl + "/stats?");
        urlBuilder.append("start=").append(URLEncoder.encode(start.format(FORMATTER), StandardCharsets.UTF_8));
        urlBuilder.append("&end=").append(URLEncoder.encode(end.format(FORMATTER), StandardCharsets.UTF_8));

        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                urlBuilder.append("&uris=").append(URLEncoder.encode(uri, StandardCharsets.UTF_8));
            }
        }

        urlBuilder.append("&unique=").append(unique);

        String url = urlBuilder.toString(); // собрали URL

        ResponseEntity<List<ViewStatsDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ViewStatsDto>>() {
                }
        ); // отправили запрос

        return response.getBody();
    }
}