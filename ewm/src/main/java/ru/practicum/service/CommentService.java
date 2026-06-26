package ru.practicum.service;

import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UpdateCommentRequest;

import java.util.List;

public interface CommentService {

    CommentDto createComment(Long userId, Long eventId, NewCommentDto dto);

    CommentDto updateComment(Long userId, Long eventId, UpdateCommentRequest dto);

    void deleteComment(Long userId, Long eventId, Long commentId);

    List<CommentDto> getUserComments(Long userId, int from, int size);

    List<CommentDto> getEventComments(Long eventId, int from, int size);

    CommentDto getComment(Long eventId, Long commentId);

    void deleteCommentByAdmin(Long commentId);
}