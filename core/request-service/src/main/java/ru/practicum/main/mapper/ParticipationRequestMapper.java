package ru.practicum.main.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.main.dto.request.ParticipationRequestDto;
import ru.practicum.main.model.ParticipationRequest;

@UtilityClass
public class ParticipationRequestMapper {

    public ParticipationRequestDto toDto(ParticipationRequest request) {
        ParticipationRequestDto dto = new ParticipationRequestDto();

        dto.setId(request.getId());
        dto.setEvent(request.getEventId());
        dto.setRequester(request.getRequestorId());
        dto.setStatus(request.getStatus());
        dto.setCreated(request.getCreated());

        return dto;
    }
}
