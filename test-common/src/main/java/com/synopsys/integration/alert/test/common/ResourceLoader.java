/*
 * test-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.test.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;

public class ResourceLoader {
    public static final String DEFAULT_PROPERTIES_FILE_LOCATION = "test.properties";
    public static final File RESOURCE_DIR = new File(ResourceLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "../../../../src/test/resources/");
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
        ClassPathResource classPathResource = new ClassPathResource(resourceLocation);
        try (InputStream iStream = classPathResource.getInputStream()) {
            properties.load(iStream);
        }
        return properties;
    }

}
