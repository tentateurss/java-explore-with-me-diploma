package ru.practicum.main.controller.internalapi;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.main.dto.event.EventSearchParams;
import ru.practicum.main.dto.event.EventShortDto;
import ru.practicum.main.service.event.PublicEventService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.stats.dto.util.DateTimeFormatters.PATTERN;

@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
public class FilteredEventsController {

    private final PublicEventService publicEventService;

    @GetMapping("/by-authors")
    public List<EventShortDto> getEventsByAuthors(
            @RequestParam("authorIds") List<Long> authorIds,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = PATTERN) LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = PATTERN) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {

        EventSearchParams params = new EventSearchParams(
                null, null, null,
                rangeStart, rangeEnd,
                false, null,
                from, size,
                request
        );

        return publicEventService.getEventsByAuthorIds(params, authorIds);
    }
}
