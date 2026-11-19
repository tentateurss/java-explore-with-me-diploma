package ru.practicum.stats.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import ru.practicum.stats.client.exceptions.StatsClientRequestException;
import ru.practicum.stats.client.exceptions.StatsServerException;
import ru.practicum.stats.dto.ErrorResponse;
import ru.practicum.stats.dto.HitRequestDto;
import ru.practicum.stats.dto.ViewStats;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static ru.practicum.stats.dto.util.DateTimeFormatters.STANDARD;

@Slf4j
public class StatsClient {

    private final RestClient.Builder restClientBuilder;
    private final DiscoveryClient discoveryClient;
    private final ObjectMapper objectMapper;

    public StatsClient(DiscoveryClient discoveryClient,
                       RestClient.Builder restClientBuilder,
                       ObjectMapper objectMapper) {
        this.discoveryClient = discoveryClient;
        this.restClientBuilder = restClientBuilder;
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<Void> saveHit(String app, String uri, String ip, LocalDateTime timestamp) {
        log.info("stats-client: Сохраняем хит: app={}, uri={}, ip={}", app, uri, ip);

        String timestampStr = timestamp.format(STANDARD);

        HitRequestDto hit = HitRequestDto.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(timestampStr)
                .build();

        return buildClient().post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(hit)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, ((request, response) ->
                        throwCustomServerException(response)))
                .onStatus(HttpStatusCode::is4xxClientError, ((request, response) ->
                        throwCustomClientRequestException(response)))
                .toBodilessEntity();
    }

    public ResponseEntity<List<ViewStats>> getStats(String start, String end,
                                                    @Nullable String[] uris, @Nullable Boolean unique) {

        log.info("stats-client: Получен запрос на получение статистики.");
        log.info("=== start={}, end={}, uris={}, unique={}", start, end,
                uris != null ? Arrays.toString(uris) : "null", unique);

        ResponseEntity<List<ViewStats>> response = buildClient().get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/stats")
                            .queryParam("start", start)
                            .queryParam("end", end);

                    if (uris != null && uris.length > 0) {
                        for (String uriStr : uris) {
                            builder.queryParam("uris", uriStr);
                        }
                    }

                    if (unique != null) {
                        builder.queryParam("unique", unique);
                    }
                    return builder.build();
                })
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, ((request, errorResponse) ->
                        throwCustomServerException(errorResponse)))
                .onStatus(HttpStatusCode::is4xxClientError, ((request, errorResponse) ->
                        throwCustomClientRequestException(errorResponse)))
                .toEntity(new ParameterizedTypeReference<>() {
                });

        log.info("=== STATS RESPONSE BODY: {}", response.getBody());
        return response;
    }

    private void throwCustomServerException(ClientHttpResponse response) {
        try {
            ErrorResponse resp = objectMapper.readValue(response.getBody(), ErrorResponse.class);
            String error = resp.getMessage();
            throw new StatsServerException(String.format("Ошибка сервера статистики: %s.",
                    error != null ? error : "детали неизвестны"));
        } catch (IOException e) {
            log.error("stats-client: не удалось прочитать сообщение об ошибке. Код ошибки: 5хх.");
            throw new StatsServerException("Неизвестная ошибка сервера статистики.");
        }
    }

    private void throwCustomClientRequestException(ClientHttpResponse response) {
        try {
            ErrorResponse resp = objectMapper.readValue(response.getBody(), ErrorResponse.class);
            String error = resp.getMessage();
            throw new StatsClientRequestException(String.format("Ошибка обработки запроса: %s.",
                    error != null ? error : "детали неизвестны"));
        } catch (IOException e) {
            log.error("stats-client: не удалось прочитать сообщение об ошибке. Код ошибки: 4хх.");
            throw new StatsClientRequestException("Неизвестная ошибка обработки запроса к серверу статистики.");
        }
    }

    private RestClient buildClient() {
        List<ServiceInstance> instances = discoveryClient.getInstances("stats-server");

        if (instances.isEmpty()) {
            throw new StatsServerException(
                    "Сервис stats-server не найден в Eureka"
            );
        }

        String baseUrl = instances.get(0).getUri().toString();

        log.debug("Адрес stats-server из Eureka: {}", baseUrl);

        return restClientBuilder.clone()
                .baseUrl(baseUrl)
                .build();
    }
}