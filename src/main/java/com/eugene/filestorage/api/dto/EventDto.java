package com.eugene.filestorage.api.dto;

import com.eugene.filestorage.entity.EventStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class EventDto {
    private Integer id;
    private Integer userId;
    private Integer fileId;
    private EventStatus status;
    private LocalDateTime timestamp;
}
