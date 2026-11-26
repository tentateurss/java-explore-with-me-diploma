package ru.practicum.main.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.main.dto.event.EventRequestInfoDto;

@FeignClient(
        name = "event-service",
        contextId = "requestEventClient"
)
public interface RequestEventClient {

    @GetMapping("/internal/events/{eventId}/request-info")
    EventRequestInfoDto getRequestInfo(@PathVariable("eventId") Long eventId);
}
