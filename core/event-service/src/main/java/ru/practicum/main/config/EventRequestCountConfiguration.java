package ru.practicum.main.config;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.main.client.RequestCountClient;
import ru.practicum.main.service.event.ConfirmedRequestCounter;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Configuration
@ConditionalOnProperty(
        name = "spring.application.name",
        havingValue = "event-service"
)
public class EventRequestCountConfiguration {

    @Bean
    public ConfirmedRequestCounter confirmedRequestCounter(
            RequestCountClient requestCountClient) {

        return eventIds -> {
            if (eventIds == null || eventIds.isEmpty()) {
                return Map.of();
            }

            List<Long> ids = eventIds.stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            if (ids.isEmpty()) {
                return Map.of();
            }

            Map<Long, Long> result = new LinkedHashMap<>();

            for (Long id : ids) {
                result.put(id, 0L);
            }

            try {
                Map<Long, Long> remoteCounts =
                        requestCountClient.getConfirmedCounts(ids);

                if (remoteCounts != null) {
                    for (Long id : ids) {
                        Long count = remoteCounts.get(id);

                        if (count != null) {
                            result.put(id, count);
                        }
                    }
                }

                return result;

            } catch (FeignException exception) {
                int status = exception.status();

                if (status == -1 || status >= 500) {
                    log.warn(
                            "request-service unavailable; " +
                            "confirmed counts default to zero, status={}",
                            status
                    );

                    return result;
                }

                throw exception;
            }
        };
    }
}
