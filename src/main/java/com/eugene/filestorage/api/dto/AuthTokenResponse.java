package com.eugene.filestorage.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AuthTokenResponse {
    private String token;
}
