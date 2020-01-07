/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

    public String getTemplatePath(String channelDirectory) throws AlertException {
        return getPath(alertProperties.getAlertTemplatesDir(), channelDirectory);
    }

    public String getImagePath(String channelDirectory) throws AlertException {
        return getPath(alertProperties.getAlertImagesDir(), channelDirectory);
    }

    private String getPath(String directory, String subDirectory) throws AlertException {
        if (StringUtils.isNotBlank(directory)) {
            return String.format("%s/%s", directory, subDirectory);
        }
        throw new AlertException(String.format("Could not find the resource directory '%s'", directory));
    }
}
