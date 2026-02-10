package com.eugene.filestorage.api.mapper;

import com.eugene.filestorage.api.dto.EventDto;
import com.eugene.filestorage.api.dto.FileDto;
import com.eugene.filestorage.api.dto.UserDto;
import com.eugene.filestorage.entity.EventEntity;
import com.eugene.filestorage.entity.FileEntity;
import com.eugene.filestorage.entity.UserEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Mappers {

    public static UserDto toDto(UserEntity user) {
        return new UserDto()
                .setId(user.getId())
                .setUsername(user.getUsername())
                .setStatus(user.getStatus());
    }

    public static FileDto toDto(FileEntity file) {
        return new FileDto()
                .setId(file.getId())
                .setName(file.getName())
                .setLocation(file.getLocation())
                .setStatus(file.getStatus());
    }

    public static EventDto toDto(EventEntity event) {
        Integer userId = event.getUser() == null ? null : event.getUser().getId();
        Integer fileId = event.getFile() == null ? null : event.getFile().getId();
        return new EventDto()
                .setId(event.getId())
                .setUserId(userId)
                .setFileId(fileId)
                .setStatus(event.getStatus())
                .setTimestamp(event.getTimestamp());
    }
}
