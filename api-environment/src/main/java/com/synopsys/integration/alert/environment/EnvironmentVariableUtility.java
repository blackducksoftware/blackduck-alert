/*
 * api-environment
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.environment;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
public class EnvironmentVariableUtility {
    private final Environment environment;

    @Autowired
    public EnvironmentVariableUtility(Environment environment) {
        this.environment = environment;
    }

    public String convertKeyToProperty(DescriptorKey descriptorKey, String key) {
        String keyUnderscores = key.replace(".", "_");
        return String.join("_", "alert", descriptorKey.getUniversalKey(), keyUnderscores).toUpperCase();
    }

    public boolean hasEnvironmentValue(String propertyKey) {
        String value = System.getProperty(propertyKey);
        return StringUtils.isNotBlank(value) || environment.containsProperty(propertyKey);
    }

    public Optional<String> getEnvironmentValue(String propertyKey) {
        return getEnvironmentValue(propertyKey, null);
    }

    public Optional<String> getEnvironmentValue(String propertyKey, String defaultValue) {
        String value = System.getProperty(propertyKey);
        if (StringUtils.isBlank(value)) {
            value = environment.getProperty(propertyKey);
        }
        boolean propertyIsSet = System.getProperties().contains(propertyKey) || environment.containsProperty(propertyKey);
        if (!propertyIsSet && null != defaultValue && StringUtils.isBlank(value)) {
            value = defaultValue;
        }
        return Optional.ofNullable(value);
    }

}
