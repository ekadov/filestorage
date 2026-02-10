package com.eugene.filestorage.api.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ApiError handle(ResponseStatusException exception, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        return new ApiError()
                .setTimestamp(Instant.now())
                .setStatus(status.value())
                .setError(status.getReasonPhrase())
                .setMessage(exception.getReason())
                .setPath(exchange.getRequest().getPath().value()
                );
    }
}
