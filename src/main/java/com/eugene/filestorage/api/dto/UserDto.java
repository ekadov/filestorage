package com.eugene.filestorage.api.dto;

import com.eugene.filestorage.entity.UserStatus;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserDto {
    private Integer id;
    private String username;
    private UserStatus status;
}
