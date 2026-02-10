package com.eugene.filestorage.controller;

import com.eugene.filestorage.api.dto.EventDto;
import com.eugene.filestorage.api.mapper.Mappers;
import com.eugene.filestorage.security.Role;
import com.eugene.filestorage.security.SecurityUtils;
import com.eugene.filestorage.service.EventService;
import com.eugene.filestorage.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/events")
@AllArgsConstructor
public class EventController {
    private final EventService events;
    private final UserService users;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR','USER')")
    public Flux<EventDto> list() {
        return SecurityUtils.currentRole()
                .flatMapMany(role -> {
                    if (role == Role.ADMIN || role == Role.MODERATOR) {
                        return events.getAll().map(Mappers::toDto);
                    }
                    return SecurityUtils.currentUsername()
                            .flatMap(users::getByUsername)
                            .flatMapMany(user -> events.getByUserId(user.getId()).map(Mappers::toDto));
                });
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR','USER')")
    public Mono<EventDto> byId(@PathVariable Integer id) {
        return SecurityUtils.currentRole()
                .flatMap(role -> {
                    if (role == Role.ADMIN || role == Role.MODERATOR) {
                        return events.getById(id).map(Mappers::toDto);
                    }
                    return SecurityUtils.currentUsername()
                            .flatMap(users::getByUsername)
                            .flatMap(user -> events.getById(id).flatMap(event -> {
                                if (event.getUser() != null && user.getId().equals(event.getUser().getId())) {
                                    return Mono.just(Mappers.toDto(event));
                                }
                                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden"));
                            }));
                });
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public Mono<Void> delete(@PathVariable Integer id) {
        return events.delete(id);
    }
}
