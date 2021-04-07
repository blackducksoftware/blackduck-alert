/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
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

import com.synopsys.integration.alert.common.exception.AlertException;

@Component
public class AlertResourceLoader {
    private final AlertProperties alertProperties;

    @Autowired
    public AlertResourceLoader(AlertProperties alertProperties) {
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

    private String getPath(String directory, String subDirectory) throws AlertException {
        if (StringUtils.isNotBlank(directory)) {
            return String.format("%s/%s", directory, subDirectory);
        }
        throw new AlertException(String.format("Could not find the resource directory '%s'", directory));
    }

}
