package ru.practicum.mapper;

import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;

import java.time.LocalDateTime;

public class CommentMapper {

    public static Comment toComment(NewCommentDto dto, User author, Event event) {
        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setAuthor(author);
        comment.setEvent(event);
        comment.setCreated(LocalDateTime.now());
        comment.setEditedOn(null);
        return comment;
    }

    public static CommentDto toCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setAuthor(UserMapper.toUserShortDto(comment.getAuthor()));
        dto.setEventId(comment.getEvent().getId());
        dto.setCreated(comment.getCreated());
        dto.setEditedOn(comment.getEditedOn());
        return dto;
    }
}