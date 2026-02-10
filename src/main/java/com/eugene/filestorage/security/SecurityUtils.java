package com.eugene.filestorage.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import reactor.core.publisher.Mono;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityUtils {

    public static Mono<String> currentUsername() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getPrincipal())
                .cast(User.class)
                .map(User::getUsername);
    }

    public static Mono<Role> currentRole() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx ->
                        ctx
                                .getAuthentication()
                                .getAuthorities()
                                .stream()
                                .findFirst()
                                .orElse(null))
                .map(a -> a == null ? null : a.getAuthority())
                .map(a -> a == null ? null : a.replace("ROLE_", ""))
                .map(Role::valueOf);
    }
}
