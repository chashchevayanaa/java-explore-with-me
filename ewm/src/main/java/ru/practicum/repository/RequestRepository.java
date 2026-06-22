package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Request;
import ru.practicum.model.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByRequesterId(Long userId);

    List<Request> findByEventId(Long eventId);

    long countByEventIdAndStatus(long eventId, RequestStatus status);

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    List<Request> findByEventIdAndStatus(Long eventId, RequestStatus status);

    @Query("SELECT r.event_id, COUNT(r.id)\n" +
            "FROM requests r\n" +
            "WHERE r.event_id IN (:eventIds)\n" +
            "  AND r.status = :status\n" +
            "GROUP BY r.event_id")
    List<Object[]> countConfirmedByEventIds(@Param("eventIds") List<Long> eventIds, @Param("status") RequestStatus status);


}
