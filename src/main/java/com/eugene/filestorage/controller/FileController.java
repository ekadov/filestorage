package com.eugene.filestorage.controller;

import com.eugene.filestorage.api.dto.FileDto;
import com.eugene.filestorage.api.dto.UpdateFileRequest;
import com.eugene.filestorage.api.mapper.Mappers;
import com.eugene.filestorage.security.Role;
import com.eugene.filestorage.security.SecurityUtils;
import com.eugene.filestorage.service.EventService;
import com.eugene.filestorage.service.FileService;
import com.eugene.filestorage.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/files")
@AllArgsConstructor
public class FileController {
    private final FileService files;
    private final EventService events;
    private final UserService users;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public Flux<FileDto> listAll() {
        return files.getAll().map(Mappers::toDto);
    }

    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR','USER')")
    public Mono<FileDto> upload(@RequestPart("file") FilePart file) {
        return SecurityUtils.currentUsername()
                .flatMap(username -> files.uploadForUser(username, file))
                .map(Mappers::toDto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR','USER')")
    public Mono<FileDto> get(@PathVariable Integer id) {
        return SecurityUtils.currentRole()
                .flatMap(role -> {
                    if (role == Role.ADMIN || role == Role.MODERATOR) {
                        return files.getById(id).map(Mappers::toDto);
                    }
                    return SecurityUtils.currentUsername()
                            .flatMap(users::getByUsername)
                            .flatMap(user -> events.findOwnerUserIdForFile(id)
                                    .flatMap(ownerId -> ownerId != null && ownerId.equals(user.getId())
                                            ? files.getById(id).map(Mappers::toDto)
                                            : Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden"))
                                    ));
                });
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public Mono<FileDto> update(@PathVariable Integer id, @RequestBody UpdateFileRequest request) {
        return SecurityUtils.currentUsername()
                .flatMap(users::getByUsername)
                .flatMap(user ->
                        files.update(
                                id,
                                request.getName(),
                                request.getLocation(),
                                request.getStatus(),
                                user.getId()))
                .map(Mappers::toDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public Mono<Void> delete(@PathVariable Integer id) {
        return SecurityUtils.currentUsername()
                .flatMap(users::getByUsername)
                .flatMap(user -> files.delete(id, user.getId()));
    }
}
