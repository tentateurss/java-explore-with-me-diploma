package ru.practicum.main.service.request;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.main.client.RequestEventClient;
import ru.practicum.main.client.RequestUserClient;
import ru.practicum.main.dto.event.EventRequestInfoDto;
import ru.practicum.main.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.main.dto.user.UserDto;
import ru.practicum.main.enums.RequestStatus;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.model.ParticipationRequest;
import ru.practicum.main.repository.ParticipationRequestRepository;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParticipationRequestServiceImplTest {

    @Mock
    private RequestUserClient requestUserClient;

    @Mock
    private RequestEventClient requestEventClient;

    @Mock
    private ParticipationRequestRepository requestRepository;

    @InjectMocks
    private ParticipationRequestServiceImpl requestService;

    @ParameterizedTest
    @EnumSource(value = RequestStatus.class, names = {"CONFIRMED", "REJECTED"})
    void shouldRejectStatusChangeForRequestFromAnotherEvent(RequestStatus newStatus) {
        Long authorId = 4L;
        Long eventId = 1L;
        Long anotherEventId = 2L;
        Long requestId = 4L;

        UserDto author = new UserDto(
                authorId,
                "Test author",
                "test-author@example.com"
        );

        when(requestUserClient.getUsersByIds(List.of(authorId)))
                .thenReturn(List.of(author));

        when(requestEventClient.getRequestInfo(eventId))
                .thenReturn(new EventRequestInfoDto(
                        eventId,
                        authorId,
                        "PUBLISHED",
                        10,
                        true
                ));

        when(requestRepository.countByEventIdAndStatus(
                eventId,
                RequestStatus.CONFIRMED
        )).thenReturn(0L);

        ParticipationRequest request = new ParticipationRequest();
        request.setId(requestId);
        request.setEventId(anotherEventId);
        request.setRequestorId(7L);
        request.setStatus(RequestStatus.PENDING);

        when(requestRepository.findAllById(any()))
                .thenReturn(List.of(request));

        EventRequestStatusUpdateRequest updateRequest =
                new EventRequestStatusUpdateRequest(
                        Set.of(requestId),
                        newStatus
                );

        assertThrows(
                NotFoundException.class,
                () -> requestService.changeRequestStatus(
                        authorId,
                        eventId,
                        updateRequest
                )
        );

        assertEquals(RequestStatus.PENDING, request.getStatus());
        assertEquals(anotherEventId, request.getEventId());

        verify(requestRepository, never()).saveAll(any());
    }
}
