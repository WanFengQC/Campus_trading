package com.campus.trading.module.file.service.impl;

import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.config.properties.FileProperties;
import com.campus.trading.module.file.dto.FileUploadResponse;
import com.campus.trading.module.file.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024;
    private static final long MAX_GOODS_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    private final FileProperties fileProperties;

    public FileStorageServiceImpl(FileProperties fileProperties) {
        this.fileProperties = fileProperties;
    }

    @Override
    public FileUploadResponse uploadAvatar(MultipartFile file, Long userId) {
        return uploadByScene(file, userId, "avatars", MAX_AVATAR_SIZE, "头像大小不能超过 2MB");
    }

    @Override
    public FileUploadResponse uploadGoodsImage(MultipartFile file, Long userId) {
        return uploadByScene(file, userId, "goods", MAX_GOODS_IMAGE_SIZE, "商品图片大小不能超过 5MB");
    }

    private FileUploadResponse uploadByScene(MultipartFile file,
                                             Long userId,
                                             String scene,
                                             long maxSize,
                                             String maxSizeErrorMessage) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择文件");
        }
        if (file.getSize() > maxSize) {
            throw new BusinessException(maxSizeErrorMessage);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = resolveExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("仅支持 jpg/jpeg/png/webp/gif 格式");
        }

        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String generatedName = userId + "_" + UUID.randomUUID().toString().replace("-", "") + "." + extension;

        Path uploadRoot = resolveUploadRoot(fileProperties.getLocalUploadDir());
        Path targetDir = uploadRoot.resolve(scene).resolve(month);
        Path targetFile = targetDir.resolve(generatedName);

        try {
            Files.createDirectories(targetDir);
            file.transferTo(targetFile);
        } catch (IOException ex) {
            throw new BusinessException("文件保存失败");
        }

        String publicUrl = normalizePrefix(fileProperties.getPublicUrlPrefix()) + "/" + scene + "/" + month + "/" + generatedName;
        return FileUploadResponse.builder()
            .url(publicUrl)
            .originalFilename(StringUtils.hasText(originalFilename) ? originalFilename : generatedName)
            .size(file.getSize())
            .build();
    }

    private String resolveExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        int index = filename.lastIndexOf('.');
        if (index < 0 || index == filename.length() - 1) {
            return "";
        }
        return filename.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String normalizePrefix(String rawPrefix) {
        if (!StringUtils.hasText(rawPrefix)) {
            return "/uploads";
        }
        String prefix = rawPrefix.trim();
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        while (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix;
    }

    private Path resolveUploadRoot(String configuredDir) {
        Path configured = Paths.get(configuredDir).normalize();
        if (configured.isAbsolute()) {
            return configured;
        }

        Path cwd = Paths.get("").toAbsolutePath().normalize();
        Path backendDir = resolveBackendDir(cwd);
        return backendDir.resolve(configured).normalize();
    }

    private Path resolveBackendDir(Path cwd) {
        Path fileName = cwd.getFileName();
        if (fileName != null && "backend".equalsIgnoreCase(fileName.toString())) {
            return cwd;
        }
        Path nestedBackend = cwd.resolve("backend");
        if (Files.exists(nestedBackend) && Files.isDirectory(nestedBackend)) {
            return nestedBackend;
        }
        return cwd;
    }
}
