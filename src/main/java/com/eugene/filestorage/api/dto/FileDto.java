package com.eugene.filestorage.api.dto;

import com.eugene.filestorage.entity.FileStatus;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FileDto {
    private Integer id;
    private String name;
    private String location;
    private FileStatus status;
}
