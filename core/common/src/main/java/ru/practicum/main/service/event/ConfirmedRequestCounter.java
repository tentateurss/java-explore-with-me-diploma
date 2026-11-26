package ru.practicum.main.service.event;

import java.util.Collection;
import java.util.Map;

public interface ConfirmedRequestCounter {
    Map<Long, Long> countConfirmed(Collection<Long> eventIds);
}