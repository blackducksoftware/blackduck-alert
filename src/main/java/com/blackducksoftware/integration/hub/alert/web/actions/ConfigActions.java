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
package com.blackducksoftware.integration.hub.alert.web.actions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.annotation.SensitiveFieldFinder;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

@Transactional
public abstract class ConfigActions<D extends DatabaseEntity, R extends ConfigRestModel, W extends JpaRepository<D, Long>> {
    private final Class<D> databaseEntityClass;
    private final Class<R> configRestModelClass;
    private final W repository;
    private final ObjectTransformer objectTransformer;

    public ConfigActions(final Class<D> databaseEntityClass, final Class<R> configRestModelClass, final W repository, final ObjectTransformer objectTransformer) {
        this.databaseEntityClass = databaseEntityClass;
        this.configRestModelClass = configRestModelClass;
        this.repository = repository;
        this.objectTransformer = objectTransformer;
    }

    public boolean doesConfigExist(final String id) {
        return doesConfigExist(objectTransformer.stringToLong(id));
    }

    public boolean doesConfigExist(final Long id) {
        return id != null && repository.existsById(id);
    }

    public List<R> getConfig(final Long id) throws AlertException {
        if (id != null) {
            final Optional<D> foundEntity = repository.findById(id);
            if (foundEntity.isPresent()) {
                final R restModel = objectTransformer.databaseEntityToConfigRestModel(foundEntity.get(), configRestModelClass);
                if (restModel != null) {
                    final R maskedRestModel = maskRestModel(restModel);
                    return Arrays.asList(maskedRestModel);
                }
            }
            return Collections.emptyList();
        }
        final List<D> databaseEntities = repository.findAll();
        final List<R> restModels = objectTransformer.databaseEntitiesToConfigRestModels(databaseEntities, configRestModelClass);
        return maskRestModels(restModels);
    }

    public R maskRestModel(final R restModel) throws AlertException {
        try {
            final Class<? extends ConfigRestModel> restModelClass = restModel.getClass();
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

    public List<R> maskRestModels(final List<R> restModels) throws AlertException {
        final List<R> maskedRestModels = new ArrayList<>();
        for (final R restModel : restModels) {
            maskedRestModels.add(maskRestModel(restModel));
        }
        return maskedRestModels;
    }

    public void deleteConfig(final String id) {
        deleteConfig(objectTransformer.stringToLong(id));
    }

    public void deleteConfig(final Long id) {
        if (id != null) {
            repository.deleteById(id);
        }
    }

    public <T> T updateNewConfigWithSavedConfig(final T newConfig, final String id) throws AlertException {
        if (StringUtils.isNotBlank(id)) {
            final Long longId = objectTransformer.stringToLong(id);
            final Optional<D> savedConfig = repository.findById(longId);
            if (savedConfig.isPresent()) {
                return updateNewConfigWithSavedConfig(newConfig, savedConfig.get());
            }
        }
        return newConfig;
    }

    @SuppressWarnings(value = "rawtypes")
    public <T> T updateNewConfigWithSavedConfig(final T newConfig, final D savedConfig) throws AlertException {
        try {
            final Class newConfigClass = newConfig.getClass();

            final Set<Field> sensitiveFields = SensitiveFieldFinder.findSensitiveFields(newConfigClass);
            for (final Field field : sensitiveFields) {
                field.setAccessible(true);
                final Object value = field.get(newConfig);
                if (value != null) {
                    final String newValue = (String) value;
                    if (StringUtils.isBlank(newValue) && savedConfig != null) {
                        final Class savedConfigClass = savedConfig.getClass();
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

    public D saveNewConfigUpdateFromSavedConfig(final R restModel) throws AlertException {
        if (restModel != null && StringUtils.isNotBlank(restModel.getId())) {
            try {
                D createdEntity = objectTransformer.configRestModelToDatabaseEntity(restModel, databaseEntityClass);
                createdEntity = updateNewConfigWithSavedConfig(createdEntity, restModel.getId());
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

    public D saveConfig(final R restModel) throws AlertException {
        if (restModel != null) {
            try {
                D createdEntity = objectTransformer.configRestModelToDatabaseEntity(restModel, databaseEntityClass);
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

    public abstract String validateConfig(final R restModel) throws AlertFieldException;

    public String testConfig(final R restModel) throws IntegrationException {
        if (restModel != null && StringUtils.isNotBlank(restModel.getId())) {
            fillNewConfigWithSavedConfig(restModel, restModel.getId());
        }
        return channelTestConfig(restModel);
    }

    public <T> T fillNewConfigWithSavedConfig(final T newConfig, final String id) throws AlertException {
        if (StringUtils.isNotBlank(id)) {
            final Long longId = objectTransformer.stringToLong(id);
            final Optional<D> savedConfig = repository.findById(longId);
            if (savedConfig.isPresent()) {
                return fillNewConfigWithSavedConfig(newConfig, savedConfig.get());
            }
        }
        return newConfig;
    }

    public <T> T fillNewConfigWithSavedConfig(final T newConfig, final D savedConfig) throws AlertException {
        try {
            final Class<?> newConfigClass = newConfig.getClass();

            final Set<Field> sensitiveFields = SensitiveFieldFinder.findSensitiveFields(newConfigClass);
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

    public abstract String channelTestConfig(final R restModel) throws IntegrationException;

    /**
     * If something needs to be triggered when the configuration is changed, this method should be overriden
     */
    public void configurationChangeTriggers(@SuppressWarnings("unused") final R restModel) {

    }

    public Boolean isBoolean(final String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        final String trimmedValue = value.trim();
        return trimmedValue.equalsIgnoreCase("false") || trimmedValue.equalsIgnoreCase("true");
    }

    public Class<D> getDatabaseEntityClass() {
        return databaseEntityClass;
    }

    public Class<R> getConfigRestModelClass() {
        return configRestModelClass;
    }

    public W getRepository() {
        return repository;
    }

    public ObjectTransformer getObjectTransformer() {
        return objectTransformer;
    }

}
