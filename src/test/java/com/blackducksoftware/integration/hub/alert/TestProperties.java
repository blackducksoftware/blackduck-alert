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
package com.blackducksoftware.integration.hub.alert;

import java.util.Properties;

import org.junit.Assume;

public class TestProperties {
    private final ResourceLoader resourceLoader;
    private Properties properties;

    public TestProperties() {
        resourceLoader = new ResourceLoader();
    }

    public Properties getProperties() {
        if (properties == null) {
            try {
                // TODO add additional locations to check for properties
                properties = resourceLoader.loadProperties(ResourceLoader.DEFAULT_PROPERTIES_FILE_LOCATION);
                if (properties.isEmpty()) {
                    populatePropertiesFromEnv();
                }
            } catch (final Exception ex) {
                System.out.println("Couldn't load " + ResourceLoader.DEFAULT_PROPERTIES_FILE_LOCATION + " file!");
            }
        }

        return properties;
    }

    private void populatePropertiesFromEnv() {
        for (final TestPropertyKey key : TestPropertyKey.values()) {
            final String prop = System.getenv(key.getPropertyKey());
            if (prop != null && !prop.isEmpty()) {
                properties.put(key.getPropertyKey(), prop);
            }
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
        Assume.assumeTrue(getProperties().containsKey(propertyKey));
    }

    public boolean containsKey(final TestPropertyKey propertyKey) {
        return containsKey(propertyKey.getPropertyKey());
    }

    public boolean containsKey(final String propertyKey) {
        return getProperties().containsKey(propertyKey);
    }
}
