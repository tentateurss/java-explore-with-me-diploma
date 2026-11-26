package ru.practicum.main.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.main.dto.event.EventShortDto;

import java.util.List;

@FeignClient(
        name = "event-service",
        contextId = "subscriptionEventClient"
)
public interface SubscriptionEventClient {

    @GetMapping("/internal/events/by-authors")
    List<EventShortDto> getEventsByAuthors(
            @RequestParam("authorIds") List<Long> authorIds,
            @RequestParam(value = "rangeStart", required = false) String rangeStart,
            @RequestParam(value = "rangeEnd", required = false) String rangeEnd,
            @RequestParam(value = "from", required = false) Integer from,
            @RequestParam(value = "size", required = false) Integer size
    );
}
