/*
 * test-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.test.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;

public final class TestResourceUtils {
    public static final String DEFAULT_PROPERTIES_FILE_LOCATION = "test.properties";
    public static final File BASE_TEST_RESOURCE_DIR = new File(TestResourceUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "../../../../src/test/resources/");

    /**
     * @param resourcePath The path to the file resource. For example: If the file is in src/test/resources/dir1/dir2/file.ext, then use "dir1/dir2/file.ext"
     * @return The file contents, never null
     * @throws IOException Thrown by {@link FileUtils} if the file cannot be read
     */
    public static String readFileToString(String resourcePath) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(resourcePath);
        File jsonFile = classPathResource.getFile();
        return FileUtils.readFileToString(jsonFile, Charset.defaultCharset());
    }

    public static Properties loadProperties(String resourceLocation) throws IOException {
        Properties properties = new Properties();
        ClassPathResource classPathResource = new ClassPathResource(resourceLocation);
        try (InputStream iStream = classPathResource.getInputStream()) {
            properties.load(iStream);
        }
        return properties;
    }

    private TestResourceUtils() {
    }

}
