/*
 * blackduck-alert
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
package com.synopsys.integration.alert.workflow.startup.component;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.DescriptorKey;

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
