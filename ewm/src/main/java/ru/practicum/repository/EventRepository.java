package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
    boolean existsByCategory_Id(Long categoryId);
}
