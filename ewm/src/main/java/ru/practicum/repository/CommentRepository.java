package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Comment;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByAuthorId(Long userId, Pageable pageable);

    Page<Comment> findByEventId(Long eventId, Pageable pageable);

    boolean existsByAuthorIdAndEventId(Long authorId, Long eventId);

    Optional<Comment> findByAuthorIdAndEventId(Long author, Long userId);

}