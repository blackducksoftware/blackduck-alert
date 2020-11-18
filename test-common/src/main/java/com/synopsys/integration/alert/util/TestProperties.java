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
package com.synopsys.integration.alert.util;

import java.util.Properties;

import org.junit.jupiter.api.Assumptions;

public class TestProperties {
    private final ResourceLoader resourceLoader;
    private Properties properties;
    private String propertiesLocation;

    public TestProperties() {
        resourceLoader = new ResourceLoader();
        propertiesLocation = ResourceLoader.DEFAULT_PROPERTIES_FILE_LOCATION;
        loadProperties();
    }

    public TestProperties(final String propertiesLocation) {
        resourceLoader = new ResourceLoader();
        this.propertiesLocation = propertiesLocation;
        loadProperties();
    }

    public void setPropertiesLocation(final String newPropertiesLocation) {
        properties = null;
        propertiesLocation = newPropertiesLocation;
        loadProperties();
    }

    public Properties getProperties() {
        loadProperties();
        return properties;
    }

    public void loadProperties() {
        if (properties == null) {
            properties = new Properties();
            try {
                properties = resourceLoader.loadProperties(propertiesLocation);
            } catch (final Exception ex) {
                System.out.println("Couldn't load " + propertiesLocation + " file!");
                System.out.println("Reading from the environment...");
            }
            populatePropertiesFromEnv();
        }
    }

    public String getProperty(final TestPropertyKey propertyKey) {
        return getProperty(propertyKey.getPropertyKey());
    }

    public String getProperty(final String propertyKey) {
        assumeTrue(propertyKey);
        return getProperties().getProperty(propertyKey);
    }

    public void assumeTrue(final TestPropertyKey propertyKey) {
        assumeTrue(propertyKey.getPropertyKey());
    }

    public void assumeTrue(final String propertyKey) {
        Assumptions.assumeTrue(getProperties().containsKey(propertyKey));
    }

    public boolean containsKey(final TestPropertyKey propertyKey) {
        return containsKey(propertyKey.getPropertyKey());
    }

    public boolean containsKey(final String propertyKey) {
        return getProperties().containsKey(propertyKey);
    }

    private void populatePropertiesFromEnv() {
        for (final TestPropertyKey key : TestPropertyKey.values()) {
            final String prop = System.getenv(key.name());
            if (prop != null && !prop.isEmpty()) {
                properties.put(key.getPropertyKey(), prop);
            }
        }
    }
}
