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
package com.synopsys.integration.alert.web.config.actions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.annotation.SensitiveFieldFinder;
import com.synopsys.integration.alert.common.descriptor.config.RestApi;
import com.synopsys.integration.alert.common.descriptor.config.TypeConverter;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.RepositoryAccessor;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.exception.IntegrationException;

public class DescriptorConfigActions {
    private final ContentConverter contentConverter;

    public DescriptorConfigActions(final ContentConverter contentConverter) {
        this.contentConverter = contentConverter;
    }

    public boolean doesConfigExist(final String id, final RestApi restApi) {
        return doesConfigExist(contentConverter.getLongValue(id), restApi);
    }

    public boolean doesConfigExist(final Long id, final RestApi restApi) {
        return id != null && restApi.readEntity(id).isPresent();
    }

    public List<? extends Config> getConfig(final Long id, final RestApi restApi) throws AlertException {
        if (id != null) {
            final Config config = getConfigById(id, restApi);
            if (config != null) {
                return Arrays.asList(config);
            }
            return Collections.emptyList();
        }
        return getConfigs(restApi);
    }

    public Config getConfigById(final Long id, final RestApi restApi) throws AlertException {
        final Optional<? extends DatabaseEntity> foundEntity = restApi.readEntity(id);
        if (foundEntity.isPresent()) {
            final Config restModel = restApi.populateConfigFromEntity(foundEntity.get());
            if (restModel != null) {
                final Config maskedRestModel = maskRestModel(restModel);
                return maskedRestModel;
            }
        }
        return null;
    }

    public List<? extends Config> getConfigs(final RestApi restApi) throws AlertException {
        final List<? extends DatabaseEntity> databaseEntities = restApi.readEntities();
        final List<Config> restModels = getConvertedRestModels(databaseEntities, restApi.getTypeConverter());
        return maskRestModels(restModels);
    }

    public void deleteConfig(final String id, final RestApi restApi) {
        deleteConfig(contentConverter.getLongValue(id), restApi);
    }

    public void deleteConfig(final Long id, final RestApi restApi) {
        if (id != null) {
            restApi.deleteEntity(id);
        }
    }

    public DatabaseEntity saveConfig(final Config config, final RestApi restApi) {
        if (config != null) {
            final DatabaseEntity createdEntity = restApi.populateEntityFromConfig(config);
            if (createdEntity != null) {
                final DatabaseEntity savedEntity = restApi.saveEntity(createdEntity);
                return savedEntity;
            }
        }
        return null;
    }

    public String validateConfig(final Config config, final RestApi restApi) throws AlertFieldException {
        final Map<String, String> fieldErrors = Maps.newHashMap();
        restApi.validateConfig(config, fieldErrors);
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    public String testConfig(final Config config, final RestApi restApi) throws IntegrationException {
        final DatabaseEntity entity = restApi.populateEntityFromConfig(config);
        restApi.testConfig(entity);
        return "Succesfully sent test message.";
    }

    public DatabaseEntity updateConfig(final Config config, final RestApi restApi) throws AlertException {
        if (config != null && StringUtils.isNotBlank(config.getId())) {
            try {
                DatabaseEntity createdEntity = restApi.populateEntityFromConfig(config);
                final DatabaseEntity savedEntity = getSavedEntity(config.getId(), restApi.getRepositoryAccessor());
                createdEntity = updateEntityWithSavedEntity(createdEntity, savedEntity);
                if (createdEntity != null) {
                    final DatabaseEntity updatedEntity = restApi.saveEntity(createdEntity);
                    return updatedEntity;
                }
            } catch (final Exception e) {
                throw new AlertException(e.getMessage(), e);
            }
        }
        return null;
    }

    public DatabaseEntity getSavedEntity(final String id, final RepositoryAccessor repositoryAccessor) throws AlertException {
        if (StringUtils.isNotBlank(id)) {
            final Long longId = contentConverter.getLongValue(id);
            final Optional<? extends DatabaseEntity> savedConfig = repositoryAccessor.readEntity(longId);
            if (savedConfig.isPresent()) {
                return savedConfig.get();
            }
        }
        return null;
    }

    public <T> T updateEntityWithSavedEntity(final T entity, final DatabaseEntity savedEntity) throws AlertException {
        try {
            final Class<?> newConfigClass = entity.getClass();

            final Set<Field> sensitiveFields = SensitiveFieldFinder.findSensitiveFields(newConfigClass);
            for (final Field field : sensitiveFields) {
                field.setAccessible(true);
                final Object value = field.get(entity);
                if (value == null || StringUtils.isBlank(value.toString())) {
                    if (savedEntity != null) {
                        final Class<?> savedConfigClass = savedEntity.getClass();
                        Field savedField = null;
                        try {
                            savedField = savedConfigClass.getDeclaredField(field.getName());
                        } catch (final NoSuchFieldException e) {
                            continue;
                        }
                        savedField.setAccessible(true);
                        final String savedValue = (String) savedField.get(savedEntity);
                        field.set(entity, savedValue);
                    }
                }
            }
        } catch (final SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new AlertException(e.getMessage(), e);
        }

        return entity;
    }

    public ContentConverter getContentConverter() {
        return contentConverter;
    }

    private List<Config> getConvertedRestModels(final List<? extends DatabaseEntity> entities, final TypeConverter typeConverter) throws AlertException {
        final List<Config> restModels = new ArrayList<>(entities.size());
        for (final DatabaseEntity entity : entities) {
            restModels.add(typeConverter.populateConfigFromEntity(entity));
        }
        return restModels;
    }

    private Config maskRestModel(final Config config) throws AlertException {
        final Class<? extends Config> restModelClass = config.getClass();
        try {
            final Set<Field> sensitiveFields = SensitiveFieldFinder.findSensitiveFields(restModelClass);

            for (final Field sensitiveField : sensitiveFields) {
                boolean isFieldSet = false;
                sensitiveField.setAccessible(true);
                final Object sensitiveFieldValue = sensitiveField.get(config);
                if (sensitiveFieldValue != null) {
                    final String sensitiveFieldString = (String) sensitiveFieldValue;
                    if (StringUtils.isNotBlank(sensitiveFieldString)) {
                        isFieldSet = true;
                    }
                }
                sensitiveField.set(config, null);

                final Field fieldIsSet = restModelClass.getDeclaredField(sensitiveField.getName() + "IsSet");
                fieldIsSet.setAccessible(true);
                final boolean sensitiveIsSetFieldValue = (boolean) fieldIsSet.get(config);
                if (!sensitiveIsSetFieldValue) {
                    fieldIsSet.setBoolean(config, isFieldSet);
                }

            }
        } catch (final NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new AlertException(e.getMessage(), e);
        }
        return config;
    }

    private List<Config> maskRestModels(final List<Config> restModels) throws AlertException {
        final List<Config> maskedRestModels = new ArrayList<>();
        for (final Config restModel : restModels) {
            maskedRestModels.add(maskRestModel(restModel));
        }
        return maskedRestModels;
    }

}
