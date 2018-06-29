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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.annotation.SensitiveFieldFinder;
import com.blackducksoftware.integration.hub.alert.channel.manager.DistributionChannelManager;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.descriptor.Descriptor;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;
import com.google.common.collect.Maps;

@Component
public class UniversalGlobalConfigActions extends UniversalConfigActions<ConfigRestModel, Descriptor> {
    private final DistributionChannelManager distributionChannelManager;

    @Autowired
    public UniversalGlobalConfigActions(final ObjectTransformer objectTransformer, final DistributionChannelManager distributionChannelManager) {
        super(objectTransformer);
        this.distributionChannelManager = distributionChannelManager;
    }

    @Override
    public boolean doesConfigExist(final String id, final Descriptor descriptor) {
        return doesConfigExist(getObjectTransformer().stringToLong(id), descriptor);
    }

    public boolean doesConfigExist(final Long id, final Descriptor descriptor) {
        return id != null && descriptor.getGlobalRepository().existsById(id);
    }

    @Override
    public List<ConfigRestModel> getConfig(final Long id, final Descriptor descriptor) throws AlertException {
        final Class restModelClass = descriptor.getGlobalRestModelClass();
        if (id != null) {
            final Optional<DatabaseEntity> foundEntity = descriptor.getGlobalRepository().findById(id);
            if (foundEntity.isPresent()) {
                final ConfigRestModel restModel = getObjectTransformer().databaseEntityToConfigRestModel(foundEntity.get(), restModelClass);
                if (restModel != null) {
                    final ConfigRestModel maskedRestModel = maskRestModel(restModel, restModelClass);
                    return Arrays.asList(maskedRestModel);
                }
            }
            return Collections.emptyList();
        }
        final List<DatabaseEntity> databaseEntities = descriptor.getGlobalRepository().findAll();
        final List<ConfigRestModel> restModels = getObjectTransformer().databaseEntitiesToConfigRestModels(databaseEntities, restModelClass);
        return maskRestModels(restModels, restModelClass);
    }

    @Override
    public void deleteConfig(final String id, final Descriptor descriptor) {
        deleteConfig(getObjectTransformer().stringToLong(id), descriptor);
    }

    public void deleteConfig(final Long id, final Descriptor descriptor) {
        if (id != null) {
            descriptor.getGlobalRepository().deleteById(id);
        }
    }

    @Override
    public DatabaseEntity saveConfig(final ConfigRestModel restModel, final Descriptor descriptor) throws AlertException {
        if (restModel != null) {
            try {
                DatabaseEntity createdEntity = getObjectTransformer().configRestModelToDatabaseEntity(restModel, descriptor.getGlobalEntityClass());
                if (createdEntity != null) {
                    createdEntity = descriptor.getGlobalRepository().save(createdEntity);
                    return createdEntity;
                }
            } catch (final Exception e) {
                throw new AlertException(e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public String validateConfig(final ConfigRestModel restModel, final Descriptor descriptor) throws AlertFieldException {
        final Map<String, String> fieldErrors = Maps.newHashMap();
        descriptor.getGlobalConfigActions().validateConfig(restModel, fieldErrors);

        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    @Override
    public String testConfig(final ConfigRestModel restModel, final Descriptor descriptor) throws IntegrationException {
        if (descriptor.getGlobalRepository() != null) {
            final List<DatabaseEntity> globalConfigs = descriptor.getGlobalRepository().findAll();
            if (globalConfigs.size() == 1) {
                return distributionChannelManager.testGlobalConfig(descriptor.getName(), (GlobalChannelConfigEntity) globalConfigs.get(0));
            }
            return "Global Config did not have the expected number of rows: Expected 1, but found " + globalConfigs.size();
        }
        return "No global repository";
    }

    @Override
    public DatabaseEntity saveNewConfigUpdateFromSavedConfig(final ConfigRestModel restModel, final Descriptor descriptor) throws AlertException {
        if (restModel != null && StringUtils.isNotBlank(restModel.getId())) {
            try {
                DatabaseEntity createdEntity = getObjectTransformer().configRestModelToDatabaseEntity(restModel, descriptor.getGlobalEntityClass());
                createdEntity = updateNewConfigWithSavedConfig(createdEntity, restModel.getId(), descriptor);
                if (createdEntity != null) {
                    createdEntity = descriptor.getGlobalRepository().save(createdEntity);
                    return createdEntity;
                }
            } catch (final Exception e) {
                throw new AlertException(e.getMessage(), e);
            }
        }
        return null;
    }

    public DatabaseEntity updateNewConfigWithSavedConfig(final DatabaseEntity newConfig, final String id, final Descriptor descriptor) throws AlertException {
        if (StringUtils.isNotBlank(id)) {
            final Long longId = getObjectTransformer().stringToLong(id);
            final Optional<DatabaseEntity> savedConfig = descriptor.getGlobalRepository().findById(longId);
            if (savedConfig.isPresent() && DatabaseEntity.class.isAssignableFrom(descriptor.getGlobalEntityClass())) {
                return updateNewConfigWithSavedConfig(newConfig, savedConfig.get(), descriptor.getGlobalEntityClass());
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

    public ConfigRestModel maskRestModel(final ConfigRestModel restModel, final Class restModelClass) throws AlertException {
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

    public List<ConfigRestModel> maskRestModels(final List<ConfigRestModel> restModels, final Class restModelClass) throws AlertException {
        final List<ConfigRestModel> maskedRestModels = new ArrayList<>();
        for (final ConfigRestModel restModel : restModels) {
            maskedRestModels.add(maskRestModel(restModel, restModelClass));
        }
        return maskedRestModels;
    }

}
