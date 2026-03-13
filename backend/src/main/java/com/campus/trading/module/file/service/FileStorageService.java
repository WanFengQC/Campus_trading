package com.campus.trading.module.file.service;

import com.campus.trading.module.file.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    FileUploadResponse uploadAvatar(MultipartFile file, Long userId);

    FileUploadResponse uploadGoodsImage(MultipartFile file, Long userId);
}