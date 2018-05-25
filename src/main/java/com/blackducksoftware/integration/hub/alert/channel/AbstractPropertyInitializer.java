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
package com.blackducksoftware.integration.hub.alert.channel;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

public abstract class AbstractPropertyInitializer<E extends DatabaseEntity> {
    private final Logger logger;

    public AbstractPropertyInitializer() {
        logger = LoggerFactory.getLogger(getClass());
    }

    public abstract String getPropertyNamePrefix();

    public abstract Class<E> getEntityClass();

    public abstract ConfigRestModel getRestModelInstance();

    public abstract void save(DatabaseEntity entity);

    public abstract boolean canSetDefaultProperties();

    public void updateEntityWithDefaults(final E savedEntity, final E defaultValuesEntity) {
        final Field[] declaredFields = getEntityClass().getDeclaredFields();
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
