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
package com.blackducksoftware.integration.hub.alert.web.test.controller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.annotation.SensitiveFieldFinder;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;
import com.google.common.collect.Maps;

@Transactional
@Component
public class UniversalConfigActions<R extends ConfigRestModel> {
    private final ObjectTransformer objectTransformer;

    @Autowired
    public UniversalConfigActions(final ObjectTransformer objectTransformer) {
        this.objectTransformer = objectTransformer;
    }

    public boolean doesConfigExist(final String id, final JpaRepository<DatabaseEntity, Long> repository) {
        return doesConfigExist(objectTransformer.stringToLong(id), repository);
    }

    public boolean doesConfigExist(final Long id, final JpaRepository<DatabaseEntity, Long> repository) {
        return id != null && repository.existsById(id);
    }

    public List<R> getConfig(final Long id, final Class<R> restModelClass, final JpaRepository<DatabaseEntity, Long> repository) throws AlertException {
        if (id != null) {
            final Optional<DatabaseEntity> foundEntity = repository.findById(id);
            if (foundEntity.isPresent()) {
                final R restModel = objectTransformer.databaseEntityToConfigRestModel(foundEntity.get(), restModelClass);
                if (restModel != null) {
                    final R maskedRestModel = maskRestModel(restModel, restModelClass);
                    return Arrays.asList(maskedRestModel);
                }
            }
            return Collections.emptyList();
        }
        final List<DatabaseEntity> databaseEntities = repository.findAll();
        final List<R> restModels = objectTransformer.databaseEntitiesToConfigRestModels(databaseEntities, restModelClass);
        return maskRestModels(restModels, restModelClass);
    }

    public R maskRestModel(final R restModel, final Class<R> restModelClass) throws AlertException {
        try {
            final Set<Field> sensitiveFields = SensitiveFieldFinder.findSensitiveFields(restModelClass);

            for (final Field sensitiveField : sensitiveFields) {
                boolean isFieldSet = false;
                sensitiveField.setAccessible(true);
                final Object sensitiveFieldValue = sensitiveField.get(restModel);
                if (sensitiveFieldValue != null) {
                    final String sensitiveFieldString = (String) sensitiveFieldValue;
                    if (StringUtils.isNotBlank(sensitiveFieldString)) {
                        isFieldSet = true;
                    }
                }
                sensitiveField.set(restModel, null);

                final Field fieldIsSet = restModelClass.getDeclaredField(sensitiveField.getName() + "IsSet");
                fieldIsSet.setAccessible(true);
                final boolean sensitiveIsSetFieldValue = (boolean) fieldIsSet.get(restModel);
                if (!sensitiveIsSetFieldValue) {
                    fieldIsSet.setBoolean(restModel, isFieldSet);
                }

            }
        } catch (final NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new AlertException(e.getMessage(), e);
        }
        return restModel;
    }

    public List<R> maskRestModels(final List<R> restModels, final Class<R> restModelClass) throws AlertException {
        final List<R> maskedRestModels = new ArrayList<>();
        for (final R restModel : restModels) {
            maskedRestModels.add(maskRestModel(restModel, restModelClass));
        }
        return maskedRestModels;
    }

    public void deleteConfig(final String id, final JpaRepository<DatabaseEntity, Long> repository) {
        deleteConfig(objectTransformer.stringToLong(id), repository);
    }

    public void deleteConfig(final Long id, final JpaRepository<DatabaseEntity, Long> repository) {
        if (id != null) {
            repository.deleteById(id);
        }
    }

    public <T extends DatabaseEntity> T updateNewConfigWithSavedConfig(final T newConfig, final String id, final JpaRepository<T, Long> repository, final Class<T> entityClass) throws AlertException {
        if (StringUtils.isNotBlank(id)) {
            final Long longId = objectTransformer.stringToLong(id);
            final Optional<T> savedConfig = repository.findById(longId);
            if (savedConfig.isPresent() && DatabaseEntity.class.isAssignableFrom(entityClass)) {
                return entityClass.cast(updateNewConfigWithSavedConfig(newConfig, savedConfig.get(), entityClass));
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

    public <T extends DatabaseEntity> T saveNewConfigUpdateFromSavedConfig(final ConfigRestModel restModel, final JpaRepository<T, Long> repository, final Class<T> entityClass) throws AlertException {
        if (restModel != null && StringUtils.isNotBlank(restModel.getId())) {
            try {
                T createdEntity = objectTransformer.configRestModelToDatabaseEntity(restModel, entityClass);
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

    public DatabaseEntity saveConfig(final ConfigRestModel restModel, final JpaRepository<DatabaseEntity, Long> repository, final Class<? extends DatabaseEntity> entityClass) throws AlertException {
        if (restModel != null) {
            try {
                DatabaseEntity createdEntity = objectTransformer.configRestModelToDatabaseEntity(restModel, entityClass);
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

    public <T extends ConfigRestModel> T fillNewConfigWithSavedConfig(final T newConfig, final String id, final JpaRepository<DatabaseEntity, Long> repository, final Class<? extends DatabaseEntity> entityClass) throws AlertException {
        if (StringUtils.isNotBlank(id)) {
            final Long longId = objectTransformer.stringToLong(id);
            final Optional<DatabaseEntity> savedConfig = repository.findById(longId);
            if (savedConfig.isPresent()) {
                return fillNewConfigWithSavedConfig(newConfig, savedConfig.get(), entityClass);
            }
        }
        return newConfig;
    }

    public <T> T fillNewConfigWithSavedConfig(final T newConfig, final DatabaseEntity savedConfig, final Class<? extends DatabaseEntity> entityClass) throws AlertException {
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

    public String validateConfig(final R restModel, final SimpleConfigActions configActions) throws AlertFieldException {
        final Map<String, String> fieldErrors = Maps.newHashMap();
        configActions.validateConfig(restModel, fieldErrors);

        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
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
