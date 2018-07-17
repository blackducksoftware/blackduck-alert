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
package com.blackducksoftware.integration.alert.startup;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.channel.PropertyInitializer;
import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.descriptor.Descriptor;
import com.blackducksoftware.integration.alert.exception.AlertException;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;

@Component
public class AlertStartupInitializer {
    public final static String ALERT_PROPERTY_PREFIX = "BLACKDUCK_ALERT_";
    private final Logger logger = LoggerFactory.getLogger(AlertStartupInitializer.class);
    private final ConversionService conversionService;
    private final Environment environment;
    private final PropertyInitializer propertyInitializer;
    private final List<Descriptor> configDescriptors;

    private final List<AlertStartupProperty> alertProperties;

    @Autowired
    public AlertStartupInitializer(final PropertyInitializer propertyInitializer, final List<Descriptor> configDescriptors, final Environment environment,
            final ConversionService conversionService) {
        this.propertyInitializer = propertyInitializer;
        this.configDescriptors = configDescriptors;
        this.environment = environment;
        this.conversionService = conversionService;
        alertProperties = new ArrayList<>(50);
    }

    public void initializeConfigs() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, AlertException {
        this.configDescriptors.forEach(descriptor -> {
            final String initializerNamePrefix = descriptor.getName();
            final Set<AlertStartupProperty> startupProperties = findPropertyNames(initializerNamePrefix, descriptor);
            if (!startupProperties.isEmpty()) {
                try {
                    final ConfigRestModel restModel = descriptor.getGlobalRestModelObject();
                    final boolean propertySet = initializeConfig(initializerNamePrefix, restModel, startupProperties);
                    if (propertySet) {
                        final DatabaseEntity entity = descriptor.convertFromGlobalRestModelToGlobalConfigEntity(restModel);
                        propertyInitializer.save(entity, descriptor);
                    }
                } catch (IllegalArgumentException | SecurityException | AlertException ex) {
                    logger.error("Error initializing property manager", ex);
                }
            }

        });
    }

    private <T extends ConfigRestModel> boolean initializeConfig(final String initializerNamePrefix, final T globalRestModel, final Set<AlertStartupProperty> configProperties) {
        boolean propertySet = false;
        for (final AlertStartupProperty property : configProperties) {
            final String propertyKey = property.getPropertyKey();
            logger.debug("Checking property key {}", propertyKey);
            String value = System.getProperty(propertyKey);
            if (StringUtils.isBlank(value)) {
                logger.debug("Not found in system env, checking Spring env");
                value = environment.getProperty(propertyKey);
            }
            try {
                propertySet = setRestModelValue(value, globalRestModel, property) || propertySet;
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                logger.error("Error initializing {} ", propertyKey, ex);
            }
        }
        return propertySet;
    }

    public <T extends ConfigRestModel> boolean setRestModelValue(final String value, final T globalRestModel, final AlertStartupProperty property)
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

    private Set<AlertStartupProperty> findPropertyNames(final String initializerNamePrefix, final Descriptor descriptor) {
        if (descriptor.getGlobalEntityFields() == null) {
            return Collections.emptySet();
        }

        final String propertyNamePrefix = ALERT_PROPERTY_PREFIX + initializerNamePrefix + "_";
        final Field[] alertConfigColumns = descriptor.getGlobalEntityFields();
        final Set<AlertStartupProperty> filteredConfigColumns = new HashSet<>();
        for (final Field field : alertConfigColumns) {
            if (field.isAnnotationPresent(Column.class)) {
                final String propertyKey = (propertyNamePrefix + field.getAnnotation(Column.class).name()).toUpperCase();
                final AlertStartupProperty alertStartupProperty = new AlertStartupProperty(getClass(), propertyKey, field.getName());
                filteredConfigColumns.add(alertStartupProperty);
                alertProperties.add(alertStartupProperty);
            }
        }

        return filteredConfigColumns;
    }

    public List<AlertStartupProperty> getAlertProperties() {
        return alertProperties;
    }

    public Set<String> getAlertPropertyNameSet() {
        return alertProperties.stream().map(AlertStartupProperty::getPropertyKey).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
