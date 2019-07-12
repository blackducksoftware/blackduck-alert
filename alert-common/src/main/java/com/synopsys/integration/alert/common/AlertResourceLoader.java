package com.synopsys.integration.alert.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class AlertResourceLoader {
    private final AlertProperties alertProperties;

    @Autowired
    public AlertResourceLoader(final AlertProperties alertProperties) {
        this.alertProperties = alertProperties;
    }

    public String getResourceAsString(String path) throws IOException {
        InputStream inputStream = getInputStreamResource(path);
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
    }

    public InputStream getInputStreamResource(String path) throws IOException {
        return getResource(path).getInputStream();
    }

    public Resource getResource(String path) {
        return new ClassPathResource(path);
    }

    public String getTemplatePath(String channelDirectory) {
        return getPath(alertProperties.getAlertTemplatesDir(), channelDirectory);
    }

    public String getImagePath(String channelDirectory) {
        return getPath(alertProperties.getAlertImagesDir(), channelDirectory);
    }

    private String getPath(String directory, String subDirectory) {
        if (StringUtils.isNotBlank(directory)) {
            return String.format("%s/%s", directory, subDirectory);
        }
        return String.format("%s/src/main/resources/%s/templates", System.getProperties().getProperty("user.dir"), subDirectory);
    }
}
