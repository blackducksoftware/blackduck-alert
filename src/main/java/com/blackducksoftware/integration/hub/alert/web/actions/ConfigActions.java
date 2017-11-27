/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

public abstract class ConfigActions<D extends DatabaseEntity, R extends ConfigRestModel> {
    public final Class<D> databaseEntityClass;
    public final Class<R> configRestModelClass;
    public final JpaRepository<D, Long> repository;
    public final ObjectTransformer objectTransformer;

    public ConfigActions(final Class<D> databaseEntityClass, final Class<R> configRestModelClass, final JpaRepository<D, Long> repository, final ObjectTransformer objectTransformer) {
        this.databaseEntityClass = databaseEntityClass;
        this.configRestModelClass = configRestModelClass;
        this.repository = repository;
        this.objectTransformer = objectTransformer;
    }

    public boolean doesConfigExist(final String id) {
        return doesConfigExist(objectTransformer.stringToLong(id));
    }

    public boolean doesConfigExist(final Long id) {
        return id != null && repository.exists(id);
    }

    public List<R> getConfig(final Long id) throws AlertException {
        if (id != null) {
            final D foundEntity = repository.findOne(id);
            if (foundEntity != null) {
                final R restModel = objectTransformer.databaseEntityToConfigRestModel(foundEntity, configRestModelClass);
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

    public abstract List<String> sensitiveFields();

    public R maskRestModel(final R restModel) throws AlertException {
        try {
            final Class<? extends ConfigRestModel> restModelClass = restModel.getClass();
            for (final String fieldName : sensitiveFields()) {
                final Field field = restModelClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(restModel, "");
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
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
        if (null != id) {
            repository.delete(id);
        }
    }

    public Object updateNewConfigWithSavedConfig(final Object newConfig, final String id) throws AlertException {
        if (StringUtils.isNotBlank(id)) {
            final Long longId = objectTransformer.stringToLong(id);
            final D savedConfig = repository.findOne(longId);
            return updateNewConfigWithSavedConfig(newConfig, savedConfig);
        }
        return newConfig;
    }

    public Object updateNewConfigWithSavedConfig(final Object newConfig, final D savedConfig) throws AlertException {
        try {
            final Class newConfigClass = newConfig.getClass();
            for (final String fieldName : sensitiveFields()) {
                final Field field = newConfigClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                final String newValue = (String) field.get(newConfig);
                if (StringUtils.isBlank(newValue) && null != savedConfig) {
                    final Class savedConfigClass = savedConfig.getClass();
                    final Field savedField = savedConfigClass.getDeclaredField(fieldName);
                    savedField.setAccessible(true);
                    final String savedValue = (String) savedField.get(savedConfig);
                    field.set(newConfig, savedValue);
                }
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new AlertException(e.getMessage(), e);
        }

        return newConfig;
    }

    public D saveNewConfigUpdateFromSavedConfig(final R restModel) throws AlertException {
        if (null != restModel && StringUtils.isNotBlank(restModel.getId())) {
            try {
                D createdEntity = objectTransformer.configRestModelToDatabaseEntity(restModel, databaseEntityClass);
                createdEntity = (D) updateNewConfigWithSavedConfig(createdEntity, restModel.getId());
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
        if (null != restModel && StringUtils.isNotBlank(restModel.getId())) {
            updateNewConfigWithSavedConfig(restModel, restModel.getId());
        }
        return channelTestConfig(restModel);
    }

    public abstract String channelTestConfig(final R restModel) throws IntegrationException;

    /**
     * If something needs to be triggered when the configuration is changed, this method should be overriden
     */
    public void configurationChangeTriggers(final R restModel) {

    }

    public Boolean isBoolean(final String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        final String trimmedValue = value.trim();
        return trimmedValue.equalsIgnoreCase("false") || trimmedValue.equalsIgnoreCase("true");
    }

}
