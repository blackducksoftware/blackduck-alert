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
package com.blackducksoftware.integration.alert.web.channel.actions;

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

import com.blackducksoftware.integration.alert.ObjectTransformer;
import com.blackducksoftware.integration.alert.annotation.SensitiveFieldFinder;
import com.blackducksoftware.integration.alert.channel.DistributionChannelManager;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.descriptor.ChannelDescriptor;
import com.blackducksoftware.integration.alert.descriptor.Descriptor;
import com.blackducksoftware.integration.alert.exception.AlertException;
import com.blackducksoftware.integration.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.google.common.collect.Maps;

@Component
public class ChannelGlobalConfigActions extends ChannelConfigActions<ConfigRestModel> {
    private final DistributionChannelManager distributionChannelManager;

    @Autowired
    public ChannelGlobalConfigActions(final ObjectTransformer objectTransformer, final DistributionChannelManager distributionChannelManager) {
        super(objectTransformer);
        this.distributionChannelManager = distributionChannelManager;
    }

    @Override
    public boolean doesConfigExist(final Long id, final ChannelDescriptor descriptor) {
        return id != null && descriptor.getGlobalRepositoryAccessor().readEntity(id).isPresent();
    }

    @Override
    public List<ConfigRestModel> getConfig(final Long id, final ChannelDescriptor descriptor) throws AlertException {
        if (id != null) {
            final Optional<? extends DatabaseEntity> foundEntity = descriptor.getGlobalRepositoryAccessor().readEntity(id);
            if (foundEntity.isPresent()) {
                final ConfigRestModel restModel = descriptor.getGlobalContentConverter().populateRestModelFromDatabaseEntity(foundEntity.get());
                if (restModel != null) {
                    final ConfigRestModel maskedRestModel = maskRestModel(restModel);
                    return Arrays.asList(maskedRestModel);
                }
            }
            return Collections.emptyList();
        }
        final List<? extends DatabaseEntity> databaseEntities = descriptor.getGlobalRepositoryAccessor().readEntities();
        final List<ConfigRestModel> restModels = getConvertedRestModels(databaseEntities, descriptor);
        return maskRestModels(restModels);
    }

    private List<ConfigRestModel> getConvertedRestModels(final List<? extends DatabaseEntity> entities, final Descriptor descriptor) throws AlertException {
        final List<ConfigRestModel> restModels = new ArrayList<>(entities.size());
        for (final DatabaseEntity entity : entities) {
            restModels.add(descriptor.getGlobalContentConverter().populateRestModelFromDatabaseEntity(entity));
        }
        return restModels;
    }

    @Override
    public void deleteConfig(final Long id, final ChannelDescriptor descriptor) {
        if (id != null) {
            descriptor.getGlobalRepositoryAccessor().deleteEntity(id);
        }
    }

    @Override
    public DatabaseEntity saveConfig(final ConfigRestModel restModel, final ChannelDescriptor descriptor) throws AlertException {
        if (restModel != null) {
            try {
                final DatabaseEntity createdEntity = descriptor.getGlobalContentConverter().populateDatabaseEntityFromRestModel(restModel);
                if (createdEntity != null) {
                    final DatabaseEntity savedEntity = descriptor.getGlobalRepositoryAccessor().saveEntity(createdEntity);
                    return savedEntity;
                }
            } catch (final Exception e) {
                throw new AlertException(e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public String validateConfig(ConfigRestModel restModel, final ChannelDescriptor descriptor) throws AlertFieldException {
        final List<? extends DatabaseEntity> globalConfigs = descriptor.getGlobalRepositoryAccessor().readEntities();
        if (globalConfigs.size() == 1) {
            try {
                restModel = updateNewConfigWithSavedConfig(restModel, globalConfigs.get(0));
            } catch (final AlertException e) {
                return "Error updating config.";
            }
        }

        final Map<String, String> fieldErrors = Maps.newHashMap();
        descriptor.validateGlobalConfig(restModel, fieldErrors);

        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    @Override
    public String testConfig(ConfigRestModel restModel, final ChannelDescriptor descriptor) throws IntegrationException {
        final List<? extends DatabaseEntity> globalConfigs = descriptor.getGlobalRepositoryAccessor().readEntities();
        if (globalConfigs.size() == 1) {
            restModel = updateNewConfigWithSavedConfig(restModel, globalConfigs.get(0));
            final DatabaseEntity entity = descriptor.getGlobalContentConverter().populateDatabaseEntityFromRestModel(restModel);
            return distributionChannelManager.testGlobalConfig(entity, descriptor);
        }
        return "Global Config did not have the expected number of rows: Expected 1, but found " + globalConfigs.size();
    }

    @Override
    public DatabaseEntity saveNewConfigUpdateFromSavedConfig(final ConfigRestModel restModel, final ChannelDescriptor descriptor) throws AlertException {
        if (restModel != null && StringUtils.isNotBlank(restModel.getId())) {
            try {
                DatabaseEntity createdEntity = descriptor.getGlobalContentConverter().populateDatabaseEntityFromRestModel(restModel);
                createdEntity = updateNewConfigWithSavedConfig(createdEntity, restModel.getId(), descriptor);
                if (createdEntity != null) {
                    final DatabaseEntity savedEntity = descriptor.getGlobalRepositoryAccessor().saveEntity(createdEntity);
                    return savedEntity;
                }
            } catch (final Exception e) {
                throw new AlertException(e.getMessage(), e);
            }
        }
        return null;
    }

    public DatabaseEntity updateNewConfigWithSavedConfig(final DatabaseEntity newConfig, final String id, final ChannelDescriptor descriptor) throws AlertException {
        if (StringUtils.isNotBlank(id)) {
            final Long longId = getObjectTransformer().stringToLong(id);
            final Optional<? extends DatabaseEntity> savedConfig = descriptor.getGlobalRepositoryAccessor().readEntity(longId);
            if (savedConfig.isPresent()) {
                return updateNewConfigWithSavedConfig(newConfig, savedConfig.get());
            }
        }
        return newConfig;
    }

    public <T> T updateNewConfigWithSavedConfig(final T newConfig, final DatabaseEntity savedConfig) throws AlertException {
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

    public ConfigRestModel maskRestModel(final ConfigRestModel restModel) throws AlertException {
        final Class<? extends ConfigRestModel> restModelClass = restModel.getClass();
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

    public List<ConfigRestModel> maskRestModels(final List<ConfigRestModel> restModels) throws AlertException {
        final List<ConfigRestModel> maskedRestModels = new ArrayList<>();
        for (final ConfigRestModel restModel : restModels) {
            maskedRestModels.add(maskRestModel(restModel));
        }
        return maskedRestModels;
    }

}
