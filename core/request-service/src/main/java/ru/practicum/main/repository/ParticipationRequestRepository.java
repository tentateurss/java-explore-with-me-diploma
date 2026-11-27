package ru.practicum.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.main.dto.request.EventCount;
import ru.practicum.main.enums.RequestStatus;
import ru.practicum.main.model.ParticipationRequest;

import java.util.Collection;
import java.util.List;

@Repository
public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findAllByRequestorId(Long userId);

    boolean existsByRequestorIdAndEventId(Long userId, Long eventId);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    ParticipationRequest findByIdAndRequestorId(Long requestId, Long userId);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    @Query("""
            select new ru.practicum.main.dto.request.EventCount(r.eventId, count(r))
            from ParticipationRequest r
            where r.eventId in :eventIds
            and r.status = :status
            group by r.eventId
            """)
    List<EventCount> countByEventIdsAndStatus(
            @Param("eventIds") Collection<Long> eventIds,
            @Param("status") RequestStatus status
    );
}