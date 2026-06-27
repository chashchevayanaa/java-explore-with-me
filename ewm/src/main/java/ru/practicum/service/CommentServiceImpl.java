package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UpdateCommentRequest;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;
import ru.practicum.model.User;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto dto) {
        log.info("Creating comment for user id: {} on event id: {}", userId, eventId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Cannot comment on unpublished event");
        }

        if (commentRepository.existsByAuthorIdAndEventId(userId, eventId)) {
            throw new ConflictException("User has already commented on this event");
        }

        Comment comment = CommentMapper.toComment(dto, user, event);
        Comment saved = commentRepository.save(comment);

        log.info("Comment created with id: {}", saved.getId());
        return CommentMapper.toCommentDto(saved);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long eventId, UpdateCommentRequest dto) {
        log.info("Updating comment for user id: {} on event id: {}", userId, eventId);

        Comment comment = commentRepository.findByAuthorIdAndEventId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new NotFoundException("Comment not found");
        }
        comment.setText(dto.getText());
        comment.setEditedOn(LocalDateTime.now());

        Comment updated = commentRepository.save(comment);

        log.info("Comment updated with id: {}", updated.getId());
        return CommentMapper.toCommentDto(updated);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long eventId, Long commentId) {
        log.info("Deleting comment with id: {}", commentId);

        Comment comment = commentRepository.findByAuthorIdAndEventId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new NotFoundException("Comment not found");
        }

        commentRepository.delete(comment);
        log.info("Comment with id: {} deleted", commentId);

    }

    @Override
    @Transactional
    public List<CommentDto> getUserComments(Long userId, int from, int size) {
        log.info("Getting comments for user id: {}, from={}, size={}", userId, from, size);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("created").descending());
        Page<Comment> page = commentRepository.findByAuthorId(userId, pageable);

        return page.getContent().stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<CommentDto> getEventComments(Long eventId, int from, int size) {
        log.info("Getting comments for event id: {}, from={}, size={}", eventId, from, size);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("created").descending());
        Page<Comment> page = commentRepository.findByEventId(eventId, pageable);

        return page.getContent().stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

    }

    @Override
    public CommentDto getComment(Long eventId, Long commentId) {
        log.info("Getting comment id: {} for event id: {}", commentId, eventId);

        Comment comment = commentRepository.findByIdAndEventId(commentId, eventId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        return CommentMapper.toCommentDto(comment);

    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        log.info("Admin deleting comment id: {}", commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));

        commentRepository.delete(comment);
        log.info("Comment deleted by admin with id: {}", commentId);
    }

}
