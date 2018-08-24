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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.StartupComponent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.alert.workflow.PropertyInitializer;

@Component
public class AlertStartupInitializer {
    public static final String ALERT_PROPERTY_PREFIX = "BLACKDUCK_ALERT_";
    private final Logger logger = LoggerFactory.getLogger(AlertStartupInitializer.class);
    private final ConversionService conversionService;
    private final Environment environment;
    private final PropertyInitializer propertyInitializer;
    private final DescriptorMap descriptorMap;
    private final List<AlertStartupProperty> alertStartupProperties;

    @Autowired
    public AlertStartupInitializer(final PropertyInitializer propertyInitializer, final DescriptorMap descriptorMap, final Environment environment,
            final ConversionService conversionService) {
        this.propertyInitializer = propertyInitializer;
        this.descriptorMap = descriptorMap;
        this.environment = environment;
        this.conversionService = conversionService;
        alertStartupProperties = new ArrayList<>(50);
    }

    // TODO try and move this functionality to startup component and eliminate this class
    public void initializeConfigs() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, AlertException {
        descriptorMap.getStartupRestApis().forEach(descriptor -> {
            final StartupComponent startupComponent = descriptor.getStartupComponent();
            final Set<AlertStartupProperty> startupProperties = startupComponent.getGlobalEntityPropertyMapping();
            if (startupProperties != null && !startupProperties.isEmpty()) {
                try {
                    final Config restModel = startupComponent.getEmptyConfigObject();
                    final boolean propertySet = initializeConfig(restModel, startupProperties);
                    if (propertySet) {
                        final DatabaseEntity entity = descriptor.getTypeConverter().populateEntityFromConfig(restModel);
                        propertyInitializer.save(entity, descriptor.getRepositoryAccessor());
                    }
                } catch (final IllegalArgumentException | SecurityException ex) {
                    logger.error("Error initializing property manager", ex);
                }
            }

        });
    }

    private boolean initializeConfig(final Config globalRestModel, final Set<AlertStartupProperty> configProperties) {
        boolean propertySet = false;
        for (final AlertStartupProperty property : configProperties) {
            alertStartupProperties.add(property);
            final String propertyKey = property.getPropertyKey();
            logger.debug("Checking property key {}", propertyKey);
            String value = System.getProperty(propertyKey);
            if (StringUtils.isBlank(value)) {
                logger.debug("Not found in system env, checking Spring env");
                value = environment.getProperty(propertyKey);
            }
            try {
                propertySet = setRestModelValue(value, globalRestModel, property) || propertySet;
            } catch (final NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                logger.error("Error initializing {} ", propertyKey, ex);
            }
        }
        return propertySet;
    }

    public boolean setRestModelValue(final String value, final Config globalRestModel, final AlertStartupProperty property)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        if (StringUtils.isNotBlank(value)) {
            logger.debug("Found the value: {}", value);
            final Field declaredField = globalRestModel.getClass().getDeclaredField(property.getFieldName());
            final boolean accessible = declaredField.isAccessible();
            if (conversionService.canConvert(String.class, declaredField.getType())) {
                final Object convertedObject = conversionService.convert(value, declaredField.getType());
                declaredField.setAccessible(true);
                declaredField.set(globalRestModel, convertedObject);
                declaredField.setAccessible(accessible);
                return true;
            }
        }
        return false;
    }

    public List<AlertStartupProperty> getAlertStartupProperties() {
        return alertStartupProperties;
    }

    public Set<String> getAlertPropertyNameSet() {
        return alertStartupProperties.stream().map(AlertStartupProperty::getPropertyKey).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
