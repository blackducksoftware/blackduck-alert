/**
 * test-common
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
package com.synopsys.integration.alert.test.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.stream.Collectors;

public class ResourceLoader {
    public static final String DEFAULT_PROPERTIES_FILE_LOCATION = "test.properties";
    public static final File RESOURCE_DIR = new File("./src/test/resources/");
    public static final String PROPERTIES_ENV_VARIABLE = "SPRING_APPLICATION_JSON";

    @SuppressWarnings("resource")
    public String loadJsonResource(String resourceLocation) throws IOException {
        File file = new File(RESOURCE_DIR, resourceLocation);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String contents = String.join("\n", reader.lines().collect(Collectors.toList()));
        reader.close();
        return contents;
    }

    @SuppressWarnings("resource")
    public Properties loadProperties(String resourceLocation) throws IOException {
        Properties properties = new Properties();

        File propertiesFile = new File(RESOURCE_DIR, resourceLocation);
        InputStream iStream = new FileInputStream(propertiesFile);
        properties.load(iStream);
        iStream.close();
        return properties;
    }

}
