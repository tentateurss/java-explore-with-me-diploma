package ru.practicum.main.controller.internalapi;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.main.service.subscription.SubscriptionService;

import java.util.List;

@RestController
@RequestMapping("/internal/subscriptions")
@RequiredArgsConstructor
public class SubscriptionAuthorController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/{userId}/authors")
    public List<Long> getSubscribedAuthorIds(@PathVariable Long userId) {
        return subscriptionService.getSubscribedAuthorIds(userId);
    }
}
