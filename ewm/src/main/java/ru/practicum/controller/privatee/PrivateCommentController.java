package ru.practicum.controller.privatec;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UpdateCommentRequest;
import ru.practicum.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
public class PrivateCommentController {

    private final CommentService commentService;

    @GetMapping("/comments")
    public List<CommentDto> getUserComments(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /users/{}/comments - Get user comments", userId);
        return commentService.getUserComments(userId, from, size);
    }

    @PostMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody NewCommentDto dto) {
        log.info("POST /users/{}/events/{}/comments - Create comment", userId, eventId);
        return commentService.createComment(userId, eventId, dto);
    }

    @PatchMapping("/events/{eventId}/comments")
    public CommentDto updateComment(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateCommentRequest dto) {
        log.info("PATCH /users/{}/events/{}/comments - Update comment", userId, eventId);
        return commentService.updateComment(userId, eventId, dto);
    }

    @DeleteMapping("/events/{eventId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @PathVariable Long commentId) {
        log.info("DELETE /users/{}/events/{}/comments/{} - Delete comment", userId, eventId, commentId);
        commentService.deleteComment(userId, eventId, commentId);
    }
}