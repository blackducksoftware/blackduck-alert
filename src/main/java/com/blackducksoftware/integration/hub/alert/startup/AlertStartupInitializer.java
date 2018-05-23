/**
 * hub-alert
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
package com.blackducksoftware.integration.hub.alert.startup;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Table;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalChannelConfigEntity;

@Component
public class AlertStartupInitializer {
    public static String ALERT_PROPERTY_PREFIX = "blackduck.";

    private final GlobalEmailRepositoryWrapper globalEmailRepositoryWrapper;

    @Autowired
    public AlertStartupInitializer(final GlobalEmailRepositoryWrapper globalEmailRepositoryWrapper) {
        this.globalEmailRepositoryWrapper = globalEmailRepositoryWrapper;
    }

    public void initializeConfigs() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        if (globalEmailRepositoryWrapper.findAll().isEmpty()) {
            final GlobalEmailConfigEntity globalEmailConfigEntity = new GlobalEmailConfigEntity();
            initializeConfig(globalEmailConfigEntity);
            globalEmailRepositoryWrapper.save(globalEmailConfigEntity);
        }

    }

    private <T extends GlobalChannelConfigEntity> void initializeConfig(final T globalConfigEntity) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        final Set<AlertStartupProperty> configProperties = findPropertyNames(globalConfigEntity.getClass());
        globalConfigEntity.setId(1L);
        for (final AlertStartupProperty property : configProperties) {
            final String value = System.getProperty(property.getPropertyKey());
            final Field declaredField = globalConfigEntity.getClass().getDeclaredField(property.getFieldName());
            final boolean accessible = declaredField.isAccessible();

            declaredField.setAccessible(true);
            declaredField.set(globalConfigEntity, value);
            declaredField.setAccessible(accessible);
        }
    }

    public Map<String, String> getAlertEnvironmentVariables() {
        final Map<String, String> alertEnvironment = System.getenv().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(ALERT_PROPERTY_PREFIX))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return alertEnvironment;
    }

    public Set<AlertStartupProperty> findAllPropertyNames(final Set<Class<?>> globalConfigClasses) {
        final Set<AlertStartupProperty> alertProperties = new HashSet<>();
        for (final Class<?> configClass : globalConfigClasses) {
            final Set<AlertStartupProperty> specificProperties = findPropertyNames(configClass);
            alertProperties.addAll(specificProperties);
        }
        return alertProperties;
    }

    public Set<AlertStartupProperty> findPropertyNames(final Class<?> alertConfigClass) {
        final String classNamePrefix = ALERT_PROPERTY_PREFIX + getClassNamePrefix(alertConfigClass) + ".";
        final Field[] alertConfigColumns = alertConfigClass.getDeclaredFields();
        final Set<AlertStartupProperty> filteredConfigColumns = new HashSet<>();
        for (final Field field : alertConfigColumns) {
            if (field.isAnnotationPresent(Column.class)) {
                final String propertyKey = classNamePrefix + field.getAnnotation(Column.class).name().replaceAll("_", ".");
                final AlertStartupProperty alertStartupProperty = new AlertStartupProperty(getClass(), propertyKey, field.getName());
                filteredConfigColumns.add(alertStartupProperty);
            }
        }

        return filteredConfigColumns;
    }

    private String getClassNamePrefix(final Class<?> alertConfigClass) {
        String classNamePrefix = alertConfigClass.getName();
        final Table classAnnotation = alertConfigClass.getAnnotation(Table.class);
        if (classAnnotation != null) {
            classNamePrefix = classAnnotation.name().replaceAll("_", ".");
        }

        return classNamePrefix;
    }

}
