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
import java.util.ArrayList;
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
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.channel.AbstractPropertyInitializer;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

@Component
public class AlertStartupInitializer {
    private final Logger logger = LoggerFactory.getLogger(AlertStartupInitializer.class);
    public static String ALERT_PROPERTY_PREFIX = "BLACKDUCK_ALERT_";

    private final ObjectTransformer objectTransformer;
    private final Environment environment;
    private final List<AbstractPropertyInitializer<? extends DatabaseEntity>> propertyManagerList;

    private final List<AlertStartupProperty> alertProperties;

    @Autowired
    public AlertStartupInitializer(final ObjectTransformer objectTransformer, final List<AbstractPropertyInitializer<? extends DatabaseEntity>> propertyManagerList, final Environment environment) {
        this.objectTransformer = objectTransformer;
        this.propertyManagerList = propertyManagerList;
        this.environment = environment;
        alertProperties = new ArrayList<>(50);
    }

    public void initializeConfigs() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, AlertException {
        this.propertyManagerList.forEach(propertyManager -> {
            final Class<? extends DatabaseEntity> entityClass = propertyManager.getEntityClass();
            final String initializerNamePrefix = propertyManager.getPropertyNamePrefix();
            findPropertyNames(initializerNamePrefix, entityClass);
            try {
                final ConfigRestModel restModel = propertyManager.getRestModelInstance();
                initializeConfig(initializerNamePrefix, restModel, entityClass);
                final DatabaseEntity entity = objectTransformer.configRestModelToDatabaseEntity(restModel, entityClass);
                propertyManager.save(entity);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException | AlertException ex) {
                logger.error("Error initializing property manager", ex);
            }
        });
    }

    private <T extends ConfigRestModel> void initializeConfig(final String initializerNamePrefix, final T globalRestModel, final Class<? extends DatabaseEntity> globalConfigEntityClass)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        final Set<AlertStartupProperty> configProperties = findPropertyNames(initializerNamePrefix, globalConfigEntityClass);
        for (final AlertStartupProperty property : configProperties) {
            logger.info("Checking property key {}", property.getPropertyKey());
            String value = System.getProperty(property.getPropertyKey());
            if (StringUtils.isBlank(value)) {
                logger.info("Not found in system env, checking Spring env");
                value = environment.getProperty(property.getPropertyKey());
            }
            setRestModelValue(value, globalRestModel, property);
        }
    }

    public <T extends ConfigRestModel> void setRestModelValue(final String value, final T globalRestModel, final AlertStartupProperty property)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        if (value != null) {
            logger.debug("Found the value: {}", value);
            final Field declaredField = globalRestModel.getClass().getDeclaredField(property.getFieldName());
            final boolean accessible = declaredField.isAccessible();

            declaredField.setAccessible(true);
            declaredField.set(globalRestModel, value);
            declaredField.setAccessible(accessible);
        }
    }

    private Set<AlertStartupProperty> findPropertyNames(final String initializerNamePrefix, final Class<?> alertConfigClass) {
        final String propertyNamePrefix = ALERT_PROPERTY_PREFIX + initializerNamePrefix + "_";
        final Field[] alertConfigColumns = alertConfigClass.getDeclaredFields();
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
