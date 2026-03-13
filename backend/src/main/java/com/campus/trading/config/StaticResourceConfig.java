package com.campus.trading.config;

import com.campus.trading.config.properties.FileProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final FileProperties fileProperties;

    public StaticResourceConfig(FileProperties fileProperties) {
        this.fileProperties = fileProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String prefix = normalizePrefix(fileProperties.getPublicUrlPrefix());
        List<String> locations = resolveUploadLocations(fileProperties.getLocalUploadDir());
        registry.addResourceHandler(prefix + "/**")
            .addResourceLocations(locations.toArray(new String[0]));
        registry.addResourceHandler("/data/uploads/**")
            .addResourceLocations(locations.toArray(new String[0]));
    }

    private List<String> resolveUploadLocations(String configuredDir) {
        List<String> locations = new ArrayList<>();
        Path configured = Paths.get(configuredDir).normalize();
        if (configured.isAbsolute()) {
            addLocation(locations, configured);
            return locations;
        }

        Path cwd = Paths.get("").toAbsolutePath().normalize();
        Path backendDir = resolveBackendDir(cwd);
        Path primary = backendDir.resolve(configured).normalize();
        addLocation(locations, primary);

        Path legacy = cwd.resolve(configured).normalize();
        if (!legacy.equals(primary)) {
            addLocation(locations, legacy);
        }

        return locations;
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

    private void addLocation(List<String> locations, Path path) {
        String location = path.toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        locations.add(location);
    }

    private String normalizePrefix(String rawPrefix) {
        if (rawPrefix == null || rawPrefix.isBlank()) {
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
}
