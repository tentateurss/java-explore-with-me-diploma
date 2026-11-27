package ru.practicum.main.service.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.enums.RequestStatus;
import ru.practicum.main.repository.ParticipationRequestRepository;
import ru.practicum.main.service.event.ConfirmedRequestCounter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConfirmedRequestCounterImpl implements ConfirmedRequestCounter {

    private final ParticipationRequestRepository requestRepository;

    @Override
    public Map<Long, Long> countConfirmed(Collection<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> result = new HashMap<>();

        requestRepository.countByEventIdsAndStatus(eventIds, RequestStatus.CONFIRMED)
                .forEach(row -> result.put(
                row.eventId(),
                row.count()
        ));

        return result;
    }
}