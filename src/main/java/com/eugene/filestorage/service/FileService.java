package com.eugene.filestorage.service;

import com.eugene.filestorage.entity.EventStatus;
import com.eugene.filestorage.entity.FileEntity;
import com.eugene.filestorage.entity.FileStatus;
import com.eugene.filestorage.repo.FileRepository;
import com.eugene.filestorage.repo.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class FileService {
    private final FileRepository fileRepo;
    private final UserRepository userRepo;
    private final S3StorageService storage;
    private final EventService eventService;
    private final FileService self;

    public FileService(FileRepository fileRepo, UserRepository userRepo,
                       S3StorageService storage, EventService eventService,
                       @Lazy FileService self) {
        this.fileRepo = fileRepo;
        this.userRepo = userRepo;
        this.storage = storage;
        this.eventService = eventService;
        this.self = self;
    }

    public Flux<FileEntity> getAll() {
        return Mono.fromCallable(fileRepo::findAll)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }

    public Mono<FileEntity> uploadForUser(String username, FilePart filePart) {
        return Mono.fromCallable(() -> userRepo.findByUsername(username)
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found")))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(user -> storage.upload(filePart)
                        .flatMap(location -> Mono.fromCallable(() -> self.uploadForUserSync(user.getId(), filePart.filename(), location))
                                .subscribeOn(Schedulers.boundedElastic()))
                );
    }

    @Transactional
    public FileEntity uploadForUserSync(Integer userId, String filename, String location) {
        FileEntity entity = new FileEntity();
        entity.setName(filename);
        entity.setLocation(location);
        entity.setStatus(FileStatus.ACTIVE);
        FileEntity saved = fileRepo.save(entity);
        eventService.createEvent(userId, saved.getId(), EventStatus.CREATED);
        return saved;
    }

    public Mono<FileEntity> getById(Integer id) {
        return Mono.fromCallable(() -> fileRepo.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "File not found")))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<FileEntity> update(Integer id, String name, String location, FileStatus status, Integer actorUserId) {
        return Mono.fromCallable(() -> self.updateSync(id, name, location, status, actorUserId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public FileEntity updateSync(Integer id, String name, String location, FileStatus status, Integer actorUserId) {
        FileEntity entity = fileRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "File not found"));
        if (name != null) entity.setName(name);
        if (location != null) entity.setLocation(location);
        if (status != null) entity.setStatus(status);
        FileEntity saved = fileRepo.save(entity);
        eventService.createEvent(actorUserId, saved.getId(), EventStatus.UPDATED);
        return saved;
    }

    public Mono<Void> delete(Integer id, Integer actorUserId) {
        return Mono.fromRunnable(() -> self.deleteSync(id, actorUserId))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @Transactional
    public void deleteSync(Integer id, Integer actorUserId) {
        FileEntity entity = fileRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "File not found"));
        entity.setStatus(FileStatus.ARCHIVED);
        fileRepo.save(entity);
        eventService.createEvent(actorUserId, entity.getId(), EventStatus.DELETED);
    }
}
