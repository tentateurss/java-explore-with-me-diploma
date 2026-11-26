package ru.practicum.main.dto.event;

public record EventRequestInfoDto(
        Long id,
        Long initiatorId,
        String state,
        Integer participantLimit,
        Boolean requestModeration
) {
}
