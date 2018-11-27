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
package com.synopsys.integration.alert.workflow;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.RepositoryAccessor;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.workflow.startup.AlertStartupProperty;

@Component
public class PropertyInitializer {
    private final Logger logger;

    public PropertyInitializer() {
        logger = LoggerFactory.getLogger(getClass());
    }

    public void save(final DatabaseEntity entity, final RepositoryAccessor repositoryAccessor, final Map<String, AlertStartupProperty> startupProperties) {
        logger.debug("Saving global properties for entity {}", entity);
        final List<? extends DatabaseEntity> savedEntityList = repositoryAccessor.readEntities();
        if (savedEntityList == null || savedEntityList.isEmpty()) {
            logger.debug("No global entities found, saving new values.");
            repositoryAccessor.saveEntity(entity);
        } else {
            logger.debug("Found existing properties, inserting new data.");
            savedEntityList.forEach(savedEntity -> {
                updateEntityWithDefaults(savedEntity, entity, startupProperties);
                repositoryAccessor.saveEntity(savedEntity);
            });

        }
    }

    private void updateEntityWithDefaults(final DatabaseEntity savedEntity, final DatabaseEntity defaultValuesEntity, final Map<String, AlertStartupProperty> startupProperties) {
        final Class<? extends DatabaseEntity> entityClass = savedEntity.getClass();
        final Field[] declaredFields = entityClass.getDeclaredFields();
        logger.debug("Inserting {} into {}", defaultValuesEntity, savedEntity);
        for (final Field declaredField : declaredFields) {
            try {
                final boolean accessible = declaredField.isAccessible();
                declaredField.setAccessible(true);
                final Object savedValue = declaredField.get(savedEntity);
                final Object defaultValue = declaredField.get(defaultValuesEntity);
                if (savedValue == null && defaultValue != null) {
                    declaredField.set(savedEntity, defaultValue);
                } else {
                    if (startupProperties.containsKey(declaredField.getName())) {
                        final AlertStartupProperty property = startupProperties.get(declaredField.getName());
                        if (property.isAlwaysOverride()) {
                            if (defaultValue != null) {
                                logger.debug("Startup Property Override Applied for {}", property.getPropertyKey());
                                declaredField.set(savedEntity, defaultValue);
                            }
                        }
                    }
                }
                declaredField.setAccessible(accessible);
            } catch (final IllegalAccessException ex) {
                logger.error("error setting default value for field {}", declaredField.getName(), ex);
            }
        }
    }
}
