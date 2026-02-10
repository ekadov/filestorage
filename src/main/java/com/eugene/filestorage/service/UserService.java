package com.eugene.filestorage.service;

import com.eugene.filestorage.entity.UserEntity;
import com.eugene.filestorage.entity.UserStatus;
import com.eugene.filestorage.repo.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class UserService {
    private final UserRepository repo;
    private final UserService self;

    public UserService(UserRepository repo, @Lazy UserService self) {
        this.repo = repo;
        this.self = self;
    }

    public Mono<UserEntity> getById(Integer id) {
        return Mono.fromCallable(() -> repo.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found")))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<UserEntity> getByUsername(String username) {
        return Mono.fromCallable(() -> repo.findByUsername(username)
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found")))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<UserEntity> getAll() {
        return Mono.fromCallable(repo::findAll)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }

    public Mono<UserEntity> create(String username) {
        return Mono.fromCallable(() -> self.createSync(username))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public UserEntity createSync(String username) {
        UserEntity entity = new UserEntity();
        entity.setUsername(username);
        entity.setStatus(UserStatus.ACTIVE);
        return repo.save(entity);
    }

    public Mono<UserEntity> updateStatus(Integer id, UserStatus status) {
        return Mono.fromCallable(() -> self.updateStatusSync(id, status))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public UserEntity updateStatusSync(Integer id, UserStatus status) {
        UserEntity entity = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        entity.setStatus(status);
        return repo.save(entity);
    }

    public Mono<Void> delete(Integer id) {
        return Mono.fromRunnable(() -> self.deleteSync(id))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @Transactional
    public void deleteSync(Integer id) {
        repo.deleteById(id);
    }
}
