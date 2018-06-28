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
package com.blackducksoftware.integration.hub.alert.web.test.controller;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.annotation.SensitiveFieldFinder;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.descriptor.Descriptor;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

public abstract class TestConfigActions<R extends ConfigRestModel, D extends Descriptor> {
    private final ObjectTransformer objectTransformer;

    public TestConfigActions(final ObjectTransformer objectTransformer) {
        this.objectTransformer = objectTransformer;
    }

    public abstract boolean doesConfigExist(final String id, D descriptor);

    public abstract List<R> getConfig(final Long id, D descriptor) throws AlertException;

    public abstract void deleteConfig(final String id, D descriptor);

    public abstract DatabaseEntity saveConfig(final R restModel, D descriptor) throws AlertException;

    public abstract String validateConfig(final R restModel, D descriptor) throws AlertFieldException;

    public abstract String testConfig(final R restModel, final D descriptor) throws IntegrationException;

    public DatabaseEntity updateNewConfigWithSavedConfig(final DatabaseEntity newConfig, final String id, final JpaRepository<DatabaseEntity, Long> repository, final Class<? extends DatabaseEntity> entityClass) throws AlertException {
        if (StringUtils.isNotBlank(id)) {
            final Long longId = objectTransformer.stringToLong(id);
            final Optional<DatabaseEntity> savedConfig = repository.findById(longId);
            if (savedConfig.isPresent() && DatabaseEntity.class.isAssignableFrom(entityClass)) {
                return updateNewConfigWithSavedConfig(newConfig, savedConfig.get(), entityClass);
            }
        }
        return newConfig;
    }

    public DatabaseEntity updateNewConfigWithSavedConfig(final DatabaseEntity newConfig, final DatabaseEntity savedConfig, final Class<? extends DatabaseEntity> entityClass) throws AlertException {
        try {
            final Set<Field> sensitiveFields = SensitiveFieldFinder.findSensitiveFields(entityClass);
            for (final Field field : sensitiveFields) {
                field.setAccessible(true);
                final Object value = field.get(newConfig);
                if (value != null) {
                    final String newValue = (String) value;
                    if (StringUtils.isBlank(newValue) && savedConfig != null) {
                        Field savedField = null;
                        try {
                            savedField = entityClass.getDeclaredField(field.getName());
                        } catch (final NoSuchFieldException e) {
                            continue;
                        }
                        savedField.setAccessible(true);
                        final String savedValue = (String) savedField.get(savedConfig);
                        field.set(newConfig, savedValue);
                    }
                }
            }
        } catch (final SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new AlertException(e.getMessage(), e);
        }

        return newConfig;
    }

    public DatabaseEntity saveNewConfigUpdateFromSavedConfig(final ConfigRestModel restModel, final JpaRepository<DatabaseEntity, Long> repository, final Class<? extends DatabaseEntity> entityClass) throws AlertException {
        if (restModel != null && StringUtils.isNotBlank(restModel.getId())) {
            try {
                DatabaseEntity createdEntity = objectTransformer.configRestModelToDatabaseEntity(restModel, entityClass);
                createdEntity = updateNewConfigWithSavedConfig(createdEntity, restModel.getId(), repository, entityClass);
                if (createdEntity != null) {
                    createdEntity = repository.save(createdEntity);
                    return createdEntity;
                }
            } catch (final Exception e) {
                throw new AlertException(e.getMessage(), e);
            }
        }
        return null;
    }

    public R fillNewConfigWithSavedConfig(final R newConfig, final String id, final JpaRepository<DatabaseEntity, Long> repository, final Class<? extends DatabaseEntity> entityClass) throws AlertException {
        if (StringUtils.isNotBlank(id)) {
            final Long longId = objectTransformer.stringToLong(id);
            final Optional<DatabaseEntity> savedConfig = repository.findById(longId);
            if (savedConfig.isPresent()) {
                return fillNewConfigWithSavedConfig(newConfig, savedConfig.get(), entityClass);
            }
        }
        return newConfig;
    }

    public R fillNewConfigWithSavedConfig(final R newConfig, final DatabaseEntity savedConfig, final Class<? extends DatabaseEntity> entityClass) throws AlertException {
        try {
            final Set<Field> sensitiveFields = SensitiveFieldFinder.findSensitiveFields(entityClass);
            for (final Field field : sensitiveFields) {
                field.setAccessible(true);
                final Object value = field.get(newConfig);
                if (value == null || StringUtils.isBlank(value.toString())) {
                    if (savedConfig != null) {
                        final Class<?> savedConfigClass = savedConfig.getClass();
                        Field savedField = null;
                        try {
                            savedField = savedConfigClass.getDeclaredField(field.getName());
                        } catch (final NoSuchFieldException e) {
                            continue;
                        }
                        savedField.setAccessible(true);
                        final String savedValue = (String) savedField.get(savedConfig);
                        field.set(newConfig, savedValue);
                    }
                }
            }
        } catch (final SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new AlertException(e.getMessage(), e);
        }

        return newConfig;
    }

    public Boolean isBoolean(final String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        final String trimmedValue = value.trim();
        return trimmedValue.equalsIgnoreCase("false") || trimmedValue.equalsIgnoreCase("true");
    }

    public ObjectTransformer getObjectTransformer() {
        return objectTransformer;
    }

}
