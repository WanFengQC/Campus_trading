package com.campus.trading.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.file")
public class FileProperties {

    private String localUploadDir = "./data/uploads";
    private String publicUrlPrefix = "/uploads";

    public String getLocalUploadDir() {
        return localUploadDir;
    }

    public void setLocalUploadDir(String localUploadDir) {
        this.localUploadDir = localUploadDir;
    }

    public String getPublicUrlPrefix() {
        return publicUrlPrefix;
    }

    public void setPublicUrlPrefix(String publicUrlPrefix) {
        this.publicUrlPrefix = publicUrlPrefix;
    }
}

