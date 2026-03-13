package com.campus.trading.module.file.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileUploadResponse {

    private String url;
    private String originalFilename;
    private long size;
}

