package ru.practicum.main.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name = "subscription-service",
        contextId = "eventSubscriptionAuthorsClient"
)
public interface SubscriptionAuthorsClient {

    @GetMapping("/internal/subscriptions/{userId}/authors")
    List<Long> getSubscribedAuthorIds(@PathVariable("userId") Long userId);
}
