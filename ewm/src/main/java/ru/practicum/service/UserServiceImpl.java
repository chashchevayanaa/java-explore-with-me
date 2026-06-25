package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        log.info("Getting users: ids={}, from={}, size={}", ids, from, size);

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));

        Page<User> userPage;
        if (ids == null || ids.isEmpty()) {
            userPage = userRepository.findAll(pageable);
        } else {
            userPage = userRepository.findByIdIn(ids, pageable);
        }

        List<UserDto> result = userPage.stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());

        log.info("Found {} users", result.size());
        return result;
    }

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest dto) {
        log.info("Creating user with email: {}", dto.getEmail());
        User user = UserMapper.toUser(dto);
        User saved = userRepository.save(user);

        log.info("User created with id: {}", saved.getId());
        return UserMapper.toUserDto(saved);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user with id: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");

        }

        userRepository.deleteById(userId);
        log.info("User with id: {} deleted", userId);

    }
}
