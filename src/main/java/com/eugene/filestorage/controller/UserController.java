package com.eugene.filestorage.controller;

import com.eugene.filestorage.api.dto.UserDto;
import com.eugene.filestorage.api.mapper.Mappers;
import com.eugene.filestorage.entity.UserStatus;
import com.eugene.filestorage.security.Role;
import com.eugene.filestorage.security.SecurityUtils;
import com.eugene.filestorage.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
    private final UserService users;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR','USER')")
    public Mono<UserDto> me() {
        return SecurityUtils.currentUsername()
                .flatMap(users::getByUsername)
                .map(Mappers::toDto);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public Flux<UserDto> all() {
        return users.getAll().map(Mappers::toDto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR','USER')")
    public Mono<UserDto> byId(@PathVariable Integer id) {
        return SecurityUtils.currentRole()
                .flatMap(role -> {
                    if (role == Role.ADMIN || role == Role.MODERATOR) {
                        return users.getById(id).map(Mappers::toDto);
                    }
                    return me().flatMap(me -> {
                        if (me.getId() != null && me.getId().equals(id)) {
                            return users.getById(id).map(Mappers::toDto);
                        }
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden"));
                    });
                });
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<UserDto> create(@RequestParam String username) {
        return users.create(username).map(Mappers::toDto);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<UserDto> updateStatus(@PathVariable Integer id, @RequestParam UserStatus status) {
        return users.updateStatus(id, status).map(Mappers::toDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> delete(@PathVariable Integer id) {
        return users.delete(id);
    }
}
