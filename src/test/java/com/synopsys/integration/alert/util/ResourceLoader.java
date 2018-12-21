/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.synopsys.integration.alert.util;

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
    public String loadJsonResource(final String resourceLocation) throws IOException {
        final File file = new File(RESOURCE_DIR, resourceLocation);
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        final String contents = String.join("\n", reader.lines().collect(Collectors.toList()));
        reader.close();
        return contents;
    }

    @SuppressWarnings("resource")
    public Properties loadProperties(final String resourceLocation) throws IOException {
        final Properties properties = new Properties();

        final File propertiesFile = new File(RESOURCE_DIR, resourceLocation);
        final InputStream iStream = new FileInputStream(propertiesFile);
        properties.load(iStream);
        iStream.close();
        return properties;
    }

}
