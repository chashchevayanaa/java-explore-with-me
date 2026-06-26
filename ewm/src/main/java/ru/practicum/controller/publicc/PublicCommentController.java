package ru.practicum.controller.publicc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.service.CommentService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/events/{eventId}/comments")
public class PublicCommentController {
    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> getEventComments(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /events/{}/comments - Get event comments", eventId);
        return commentService.getEventComments(eventId, from, size);
    }

    @GetMapping("/{commentId}")
    public CommentDto getComment(
            @PathVariable Long eventId,
            @PathVariable Long commentId) {
        log.info("GET /events/{}/comments/{} - Get comment", eventId, commentId);
        return commentService.getComment(eventId, commentId);
    }
}