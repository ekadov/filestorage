package com.eugene.filestorage.service;

import com.eugene.filestorage.entity.EventEntity;
import com.eugene.filestorage.entity.EventStatus;
import com.eugene.filestorage.entity.FileEntity;
import com.eugene.filestorage.entity.UserEntity;
import com.eugene.filestorage.repo.EventRepository;
import com.eugene.filestorage.repo.FileRepository;
import com.eugene.filestorage.repo.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final EventService self;

    public EventService(EventRepository eventRepository, UserRepository userRepository,
                        FileRepository fileRepository, @Lazy EventService self) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.self = self;
    }

    public Mono<EventEntity> createEvent(Integer userId, Integer fileId, EventStatus status) {
        return Mono.fromCallable(() -> self.createEventSync(userId, fileId, status))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public EventEntity createEventSync(Integer userId, Integer fileId, EventStatus status) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "File not found"));

        EventEntity entity = new EventEntity();
        entity.setUser(user);
        entity.setFile(file);
        entity.setStatus(status);
        entity.setTimestamp(LocalDateTime.now());
        return eventRepository.save(entity);
    }

    public Mono<EventEntity> getById(Integer id) {
        return Mono.fromCallable(() -> eventRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Event not found")))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<EventEntity> getAll() {
        return Mono.fromCallable(eventRepository::findAll)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }

    public Flux<EventEntity> getByUserId(Integer userId) {
        return Mono.fromCallable(() -> eventRepository.findByUser_Id(userId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }

    public Mono<Void> delete(Integer id) {
        return Mono.fromRunnable(() -> self.deleteSync(id))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @Transactional
    public void deleteSync(Integer id) {
        eventRepository.deleteById(id);
    }

    public Mono<Integer> findOwnerUserIdForFile(Integer fileId) {
        return Mono.fromCallable(() -> eventRepository.findOwnerIdForFile(fileId).orElse(null))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
