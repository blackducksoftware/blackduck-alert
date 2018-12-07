/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.workflow.startup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.database.BaseDescriptorAccessor;
import com.synopsys.integration.alert.common.database.FieldConfigurationAccessor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.configuration.ConfigContextEnum;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor.ConfigurationModel;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.DefinedFieldModel;

@Component
public class AlertStartupInitializer {
    private final Logger logger = LoggerFactory.getLogger(AlertStartupInitializer.class);
    private final Environment environment;
    private final DescriptorMap descriptorMap;
    private final Set<String> alertStartupFields;
    private final BaseDescriptorAccessor descriptorAccessor;
    private final FieldConfigurationAccessor fieldConfigurationAccessor;

    @Autowired
    public AlertStartupInitializer(final DescriptorMap descriptorMap, final Environment environment, final BaseDescriptorAccessor descriptorAccessor, final FieldConfigurationAccessor fieldConfigurationAccessor) {
        this.descriptorMap = descriptorMap;
        this.environment = environment;
        this.descriptorAccessor = descriptorAccessor;
        this.fieldConfigurationAccessor = fieldConfigurationAccessor;
        alertStartupFields = new HashSet<>();
    }

    public void initializeConfigs(final boolean overwriteCurrentConfig) throws IllegalArgumentException, SecurityException, AlertDatabaseConstraintException {
        final Set<String> descriptorNames = descriptorMap.getDescriptorMap().keySet();
        for (final String descriptorName : descriptorNames) {
            final Map<String, String> newConfiguration = new HashMap<>();
            final List<DefinedFieldModel> fieldsForDescriptor = descriptorAccessor.getFieldsForDescriptor(descriptorName, ConfigContextEnum.GLOBAL);
            for (final DefinedFieldModel fieldModel : fieldsForDescriptor) {
                final String key = fieldModel.getKey();
                final String convertedKey = convertKeyToPropery(descriptorName, key);
                final String value = getEnvironmentValue(convertedKey);
                if (StringUtils.isNotBlank(value)) {
                    newConfiguration.put(key, value);
                }
                alertStartupFields.add(convertedKey);
            }
            if (!newConfiguration.isEmpty()) {
                final Set<ConfigurationFieldModel> fieldModels = createConfigurationFieldModels(newConfiguration);
                final Optional<ConfigurationModel> foundConfigurationModel = fieldConfigurationAccessor.getConfigurationByDescriptorNameAndContext(descriptorName, ConfigContextEnum.GLOBAL);
                if (foundConfigurationModel.isPresent() && overwriteCurrentConfig) {
                    if (overwriteCurrentConfig) {
                        fieldConfigurationAccessor.updateConfiguration(foundConfigurationModel.get().getConfigurationId(), fieldModels);
                    }
                } else {
                    fieldConfigurationAccessor.createConfiguration(descriptorName, ConfigContextEnum.GLOBAL, fieldModels);
                }
            }
        }
    }

    private Set<ConfigurationFieldModel> createConfigurationFieldModels(final Map<String, String> fields) {
        return fields.entrySet()
                   .stream()
                   .map(entry -> createConfigurationFieldModel(entry.getKey(), entry.getValue()))
                   .collect(Collectors.toSet());
    }

    private ConfigurationFieldModel createConfigurationFieldModel(final String key, final String value) {
        final ConfigurationFieldModel field = ConfigurationFieldModel.create(key);
        field.setFieldValue(value);
        return field;
    }

    private String convertKeyToPropery(final String descriptorName, final String key) {
        final String keyUnderscores = key.replace(".", "_");
        return String.join("_", "alert", descriptorName, keyUnderscores).toUpperCase();
    }

    private String getEnvironmentValue(final String propertyKey) {
        String value = System.getProperty(propertyKey);
        if (StringUtils.isBlank(value)) {
            logger.debug("Not found in system env, checking Spring env");
            value = environment.getProperty(propertyKey);
        }
        return value;
    }

    public Set<String> getAlertPropertyNameSet() {
        return alertStartupFields;
    }
}
