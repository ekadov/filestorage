package com.eugene.filestorage.api.dto;

import com.eugene.filestorage.security.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AuthTokenRequest {
    private @NotBlank String username;
    private @NotNull Role role;
}
