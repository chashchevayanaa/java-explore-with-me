package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Request;
import ru.practicum.model.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByRequesterId(Long userId);
    List<Request> findByEventId(Long eventId);
    long countByEventIdAndStatus(long eventId, RequestStatus status);
    boolean existsByEventIdAndRequesterId (Long eventId, Long requesterId);
}
