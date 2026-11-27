package ru.practicum.main.service.request;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.main.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.main.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.main.dto.request.ParticipationRequestDto;
import ru.practicum.main.dto.event.EventRequestInfoDto;
import ru.practicum.main.enums.RequestStatus;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.mapper.ParticipationRequestMapper;
import ru.practicum.main.model.ParticipationRequest;
import ru.practicum.main.dto.user.UserDto;
import ru.practicum.main.repository.ParticipationRequestRepository;
import feign.FeignException;
import ru.practicum.main.client.RequestEventClient;
import ru.practicum.main.client.RequestUserClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ParticipationRequestServiceImpl implements ParticipationRequestService {
    private final RequestUserClient requestUserClient;
    private final ParticipationRequestRepository requestRepository;
    private final RequestEventClient requestEventClient;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        checkUserExist(userId);
        return requestRepository.findAllByRequestorId(userId).stream()
                .map(ParticipationRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        log.info("Creating request for user id={}, event id={}", userId, eventId);

        EventRequestInfoDto event = getEventById(eventId);

        if (requestRepository.existsByRequestorIdAndEventId(userId, eventId)) {
            throw new ConflictException("Реквест уже существует");
        }

        if (userId.equals(event.initiatorId())) {
            throw new ConflictException("Инициатор не может запросить участие в собственном событии");
        }

        if (!"PUBLISHED".equals(event.state())) {
            throw new ConflictException("Это событие ещё не опубликовано");
        }

        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.participantLimit() != 0 && confirmedCount >= event.participantLimit()) {
            throw new ConflictException("Достигнут лимит заявок на участие");
        }

        checkUserExist(userId);
        ParticipationRequest request = new ParticipationRequest();
        request.setCreated(LocalDateTime.now());
        request.setEventId(eventId);
        request.setRequestorId(userId);

        if (!event.requestModeration() || event.participantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        request = requestRepository.save(request);
        log.info("Request created: id={}, status={}", request.getId(), request.getStatus());

        return ParticipationRequestMapper.toDto(request);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Cancelling request id={} for user id={}", requestId, userId);

        ParticipationRequest request = requestRepository.findByIdAndRequestorId(requestId, userId);
        if (request == null) {
            throw new NotFoundException("Заявка не найдена или не принадлежит пользователю");
        }
        request.setStatus(RequestStatus.CANCELED);

        request = requestRepository.save(request);
        log.info("Request cancelled: id={}", request.getId());

        return ParticipationRequestMapper.toDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        checkUserExist(userId);
        EventRequestInfoDto event = getEventById(eventId);
        if (!userId.equals(event.initiatorId())) {
            throw new NotFoundException("Событие с id=" + eventId + " пользователя с id=" + userId + " не найдено");
        }

        return requestRepository.findAllByEventId(eventId).stream()
                .map(ParticipationRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest dto) {
        log.info("Changing request status for event id={}, user id={}", eventId, userId);

        checkUserExist(userId);
        EventRequestInfoDto event = getEventById(eventId);
        if (!userId.equals(event.initiatorId())) {
            throw new NotFoundException("Событие с id=" + eventId + " пользователя с id=" + userId + " не найдено");
        }

        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        int limit = event.participantLimit();

        if (dto.getStatus() == RequestStatus.CONFIRMED) {
            if (limit != 0 && confirmedCount >= limit) {
                throw new ConflictException("Достигнут лимит заявок на участие");
            }
        }

        List<ParticipationRequest> requests = requestRepository.findAllById(dto.getRequestIds());

        if (requests.size() != dto.getRequestIds().size()) {
            throw new NotFoundException("Одна или несколько заявок не найдены");
        }

        for (ParticipationRequest request : requests) {
            if (!eventId.equals(request.getEventId())) {
                throw new NotFoundException("Заявка не принадлежит мероприятию");
            }
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Статус заявки должен быть PENDING");
            }
        }

        List<ParticipationRequest> confirmed = new ArrayList<>();
        List<ParticipationRequest> rejected = new ArrayList<>();

        for (ParticipationRequest request : requests) {
            if (dto.getStatus() == RequestStatus.CONFIRMED) {
                if (limit == 0 || confirmedCount < limit) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    confirmed.add(request);
                    confirmedCount++;
                } else {
                    request.setStatus(RequestStatus.REJECTED);
                    rejected.add(request);
                }
            } else {
                request.setStatus(RequestStatus.REJECTED);
                rejected.add(request);
            }
        }

        requestRepository.saveAll(confirmed);
        requestRepository.saveAll(rejected);

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmed.stream()
                .map(ParticipationRequestMapper::toDto)
                .collect(Collectors.toSet()));
        result.setRejectedRequests(rejected.stream()
                .map(ParticipationRequestMapper::toDto)
                .collect(Collectors.toSet()));

        log.info("Request status changed: confirmed={}, rejected={}", confirmed.size(), rejected.size());

        return result;
    }

    private void checkUserExist(Long userId) {
        List<UserDto> users = requestUserClient.getUsersByIds(List.of(userId));

        if (users == null || users.stream().noneMatch(user -> userId.equals(user.getId()))) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }

    private EventRequestInfoDto getEventById(Long eventId) {
        try {
            return requestEventClient.getRequestInfo(eventId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }
    }
}