/*
 * test-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.test.common;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assumptions;

public class TestProperties {
    private Properties properties;
    private String propertiesLocation;

    public TestProperties() {
        try {
            propertiesLocation = TestResourceUtils.createTestPropertiesCanonicalFilePath().toString();
        } catch (IOException e) {
            propertiesLocation = TestResourceUtils.DEFAULT_PROPERTIES_FILE_NAME;
        }
        loadProperties();
    }

    public TestProperties(String propertiesLocation) {
        this.propertiesLocation = propertiesLocation;
        loadProperties();
    }

    public void setPropertiesLocation(String newPropertiesLocation) {
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
                properties = TestResourceUtils.loadProperties(propertiesLocation);
            } catch (Exception ex) {
                System.out.println("Couldn't load " + propertiesLocation + " file!");
                System.out.println("Reading from the environment...");
            }
            populatePropertiesFromEnv();
        }
    }

    public String getProperty(TestPropertyKey propertyKey) {
        return getProperty(propertyKey.getPropertyKey());
    }

    public String getProperty(String propertyKey) {
        assumeTrue(propertyKey);
        return getProperties().getProperty(propertyKey);
    }

    public String getBlackDuckURL() {
        //Retrieving TEST_BLACKDUCK_PROVIDER_URL if it is not set getting BLACKDUCK_URL
        return getOptionalProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_URL)
                   .orElseGet(() -> getProperty(TestPropertyKey.BLACKDUCK_URL));
    }

    public String getBlackDuckAPIToken() {
        //Retrieving TEST_BLACKDUCK_PROVIDER_API_KEY if it is not set getting BLACKDUCK_API_TOKEN
        return getOptionalProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_API_KEY)
                   .orElseGet(() -> getProperty(TestPropertyKey.BLACKDUCK_API_TOKEN));
    }

    public Optional<String> getOptionalProperty(TestPropertyKey propertyKey) {
        return getOptionalProperty(propertyKey.getPropertyKey());
    }

    public Optional<String> getOptionalProperty(String propertyKey) {
        String property = getProperties().getProperty(propertyKey);
        return Optional.ofNullable(property).filter(StringUtils::isNotBlank);
    }

    public void assumeTrue(TestPropertyKey propertyKey) {
        assumeTrue(propertyKey.getPropertyKey());
    }

    public void assumeTrue(String propertyKey) {
        Assumptions.assumeTrue(getProperties().containsKey(propertyKey));
    }

    public boolean containsKey(TestPropertyKey propertyKey) {
        return containsKey(propertyKey.getPropertyKey());
    }

    public boolean containsKey(String propertyKey) {
        return getProperties().containsKey(propertyKey);
    }

    private void populatePropertiesFromEnv() {
        for (TestPropertyKey key : TestPropertyKey.values()) {
            String prop = System.getenv(key.name());
            if (prop != null && !prop.isEmpty()) {
                properties.put(key.getPropertyKey(), prop);
            }
        }
    }

}
