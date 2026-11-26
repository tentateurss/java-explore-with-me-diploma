package ru.practicum.main.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "request-service",
        contextId = "requestCountClient"
)
public interface RequestCountClient {

    @GetMapping("/internal/requests/confirmed-counts")
    Map<Long, Long> getConfirmedCounts(
            @RequestParam("eventIds") List<Long> eventIds
    );
}
