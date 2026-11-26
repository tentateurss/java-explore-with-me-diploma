package ru.practicum.main.controller.internalapi;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.main.dto.event.EventRequestInfoDto;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.model.Event;
import ru.practicum.main.repository.EventRepository;

@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
public class EventRequestInfoController {

    private final EventRepository eventRepository;

    @GetMapping("/{eventId}/request-info")
    public EventRequestInfoDto getRequestInfo(@PathVariable Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        "Событие с id=" + eventId + " не найдено"
                ));

        return new EventRequestInfoDto(
                event.getId(),
                event.getInitiatorId(),
                event.getState().name(),
                event.getParticipantLimit(),
                event.getRequestModeration()
        );
    }
}
