package ru.practicum.mapper;

import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.model.User;

public class UserMapper {

    public static User toUser(NewUserRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        return user;
    }

    public static UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getName()
        );
    }

    public static UserShortDto toUserShortDto(User user) {
        return new UserShortDto(user.getId(), user.getName());
    }
}

