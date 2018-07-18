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
package com.blackducksoftware.integration.alert.channel;

import java.lang.reflect.Field;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.descriptor.Descriptor;

@Component
public class PropertyInitializer {
    private final Logger logger;

    public PropertyInitializer() {
        logger = LoggerFactory.getLogger(getClass());
    }

    public void save(final DatabaseEntity entity, final Descriptor descriptor) {
        logger.info("Saving global properties {}", entity);
        final List<? extends DatabaseEntity> savedEntityList = descriptor.getGlobalRepositoryAccessor().readEntities();
        if (savedEntityList == null || savedEntityList.isEmpty()) {
            logger.debug("No global entities found, saving new values.");
            descriptor.getGlobalRepositoryAccessor().saveEntity(entity);
        } else {
            logger.debug("Found existing properties, inserting new data.");
            savedEntityList.forEach(savedEntity -> {
                updateEntityWithDefaults(savedEntity, entity);
                descriptor.getGlobalRepositoryAccessor().saveEntity(savedEntity);
            });

        }
    }

    private void updateEntityWithDefaults(final DatabaseEntity savedEntity, final DatabaseEntity defaultValuesEntity) {
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
                }
                declaredField.setAccessible(accessible);
            } catch (final IllegalAccessException ex) {
                logger.error("error setting default value for field {}", declaredField.getName(), ex);
            }
        }
    }
}
