package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.*;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        log.info("Creating request for user id: {} on event id: {}", userId, eventId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Event is not published");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("You cannot request participation in your own event");
        }

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Request already exists");
        }

        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        int limit = event.getParticipantLimit();
        if (limit > 0 && confirmedRequests >= limit) {
            throw new ConflictException("The participant limit has been reached");
        }

        Request request = new Request();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(user);


        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        Request saved = requestRepository.save(request);
        log.info("Request created with id: {}", saved.getId());
        return RequestMapper.toDto(saved);
    }

    @Override
    @Transactional
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Getting requests for user id: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        List<Request> requests = requestRepository.findByRequesterId(userId);

        log.info("Found {} requests for user id: {}", requests.size(), userId);
        return requests.stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Cancelling request id: {} for user id: {}", requestId, userId);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Request with id=" + requestId + " was not found for user id=" + userId);
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new ConflictException("Only pending requests can be cancelled");

        }

        request.setStatus(RequestStatus.CANCELED);
        Request cancelled = requestRepository.save(request);

        log.info("Request id: {} cancelled", requestId);
        return RequestMapper.toDto(cancelled);
    }


    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("Getting requests for event id: {} by user id: {}", eventId, userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found for user id=" + userId));

        List<Request> requests = requestRepository.findByEventId(eventId);

        log.info("Found {} requests for event id: {}", requests.size(), eventId);
        return requests.stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequest requestUpdate) {

        log.info("Updating request status for event id: {} by user id: {}", eventId, userId);


        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found for user id=" + userId));

        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        int participantLimit = event.getParticipantLimit();

        if (requestUpdate.getStatus() == RequestStatus.CONFIRMED) {
            if (confirmedRequests >= participantLimit && participantLimit != 0) {
                throw new ConflictException("The participant limit has been reached");
            }
        }

        List<Request> requests = requestRepository.findAllById(requestUpdate.getRequestIds());
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (Request request : requests) {
            if (!request.getEvent().getId().equals(eventId)) {
                throw new BadRequestException("Request with id=" + request.getId() + " does not belong to event id=" + eventId);
            }
        }

        for (Request request : requests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Request with id=" + request.getId() + " must be in PENDING status");
            }

            if (requestUpdate.getStatus() == RequestStatus.CONFIRMED) {
                if (confirmedRequests >= participantLimit && participantLimit != 0) {
                    request.setStatus(RequestStatus.REJECTED);
                    rejected.add(RequestMapper.toDto(request));
                } else {
                    request.setStatus(RequestStatus.CONFIRMED);
                    confirmedRequests++;
                    confirmed.add(RequestMapper.toDto(request));
                }
            } else if (requestUpdate.getStatus() == RequestStatus.REJECTED) {
                request.setStatus(RequestStatus.REJECTED);
                rejected.add(RequestMapper.toDto(request));
            }
        }

        requestRepository.saveAll(requests);
        log.info("Updated {} requests for event id: {}", requests.size(), eventId);

        return new EventRequestStatusUpdateResult(confirmed, rejected);
    }

}