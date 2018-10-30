/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

@Component
public class FilePersistenceUtil {
    public static final String ENCODING = "UTF-8";
    private final File parentDataDirectory;
    private final Gson gson;

    public FilePersistenceUtil(final AlertProperties alertProperties, final Gson gson) {
        this.gson = gson;
        String dataDirectory = "data/";
        if (StringUtils.isNotBlank(alertProperties.getAlertConfigHome())) {
            dataDirectory = String.format("%s/data", alertProperties.getAlertConfigHome());
        }
        this.parentDataDirectory = new File(dataDirectory);
    }

    public void writeToFile(final String fileName, final String content) throws IOException {
        FileUtils.write(createFilePath(fileName), content, ENCODING);
    }

    public void writeJsonToFile(final String fileName, final Object content) throws IOException {
        final String jsonString = gson.toJson(content);
        writeToFile(fileName, jsonString);
    }

    public String readFromFile(final String fileName) throws IOException {
        return FileUtils.readFileToString(createFilePath(fileName), ENCODING);
    }

    public <T> T readJsonFromFile(final String fileName, final Class<T> clazz) throws IOException {
        final String jsonString = readFromFile(fileName);
        return gson.fromJson(jsonString, clazz);
    }

    public boolean exists(final String fileName) {
        final File file = createFilePath(fileName);
        return file.exists();
    }

    public void delete(final String fileName) throws IOException {
        final File file = createFilePath(fileName);
        FileUtils.forceDelete(file);
    }

    private File createFilePath(final String fileName) {
        return new File(parentDataDirectory, fileName);
    }
}
