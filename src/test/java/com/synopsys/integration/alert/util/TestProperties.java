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

import java.util.Properties;

import org.junit.Assume;

public class TestProperties {
    private final ResourceLoader resourceLoader;
    private Properties properties;
    private String propertiesLocation;

    public TestProperties() {
        resourceLoader = new ResourceLoader();
        propertiesLocation = ResourceLoader.DEFAULT_PROPERTIES_FILE_LOCATION;
    }

    public TestProperties(final String propertiesLocation) {
        resourceLoader = new ResourceLoader();
        this.propertiesLocation = propertiesLocation;
    }

    public void setPropertiesLocation(final String newPropertiesLocation) {
        properties = null;
        propertiesLocation = newPropertiesLocation;
    }

    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            try {
                properties = resourceLoader.loadProperties(propertiesLocation);
                if (properties.isEmpty()) {
                    populatePropertiesFromEnv();
                }
            } catch (final Exception ex) {
                System.out.println("Couldn't load " + propertiesLocation + " file!");
                System.out.println("Reading from the environment...");
                populatePropertiesFromEnv();
            }
        }

        return properties;
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
        Assume.assumeTrue(getProperties().containsKey(propertyKey));
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
