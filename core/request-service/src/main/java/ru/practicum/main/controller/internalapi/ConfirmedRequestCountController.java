package ru.practicum.main.controller.internalapi;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.main.service.event.ConfirmedRequestCounter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/requests")
@RequiredArgsConstructor
public class ConfirmedRequestCountController {

    private final ConfirmedRequestCounter confirmedRequestCounter;

    @GetMapping("/confirmed-counts")
    public Map<Long, Long> getConfirmedCounts(
            @RequestParam("eventIds") List<Long> eventIds) {

        return confirmedRequestCounter.countConfirmed(eventIds);
    }
}
