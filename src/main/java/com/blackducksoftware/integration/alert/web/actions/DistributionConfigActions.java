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
package com.blackducksoftware.integration.alert.web.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;

import com.blackducksoftware.integration.alert.common.descriptor.DatabaseContentConverter;
import com.blackducksoftware.integration.alert.common.exception.AlertException;
import com.blackducksoftware.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.alert.web.exception.AlertFieldException;
import com.blackducksoftware.integration.alert.web.model.CommonDistributionConfig;

// TODO this class is only used by CommonDistributionConfigActions now and can probably be merged together (If we don't find a way to use the channel config actions)
@Transactional
public abstract class DistributionConfigActions<D extends DatabaseEntity, R extends CommonDistributionConfig, W extends JpaRepository<D, Long>> extends ConfigActions<D, R, W> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CommonDistributionRepository commonDistributionRepository;
    private final ConfiguredProjectsActions configuredProjectsActions;
    private final NotificationTypesActions notificationTypesActions;
    private final DatabaseContentConverter commonDistributionContentConverter;

    public DistributionConfigActions(final CommonDistributionRepository commonDistributionRepository, final W channelDistributionRepository,
            final ConfiguredProjectsActions configuredProjectsActions, final NotificationTypesActions notificationTypesActions, final DatabaseContentConverter databaseContentConverter,
            final DatabaseContentConverter commonDistributionContentConverter) {
        super(channelDistributionRepository, databaseContentConverter);
        this.commonDistributionRepository = commonDistributionRepository;
        this.configuredProjectsActions = configuredProjectsActions;
        this.notificationTypesActions = notificationTypesActions;
        this.commonDistributionContentConverter = commonDistributionContentConverter;
    }

    @Override
    public List<R> getConfig(final Long id) throws AlertException {
        if (id != null) {
            final Optional<D> foundEntity = getRepository().findById(id);
            if (foundEntity.isPresent()) {
                return Arrays.asList(constructRestModel(foundEntity.get()));
            }
            return Collections.emptyList();
        }
        return constructRestModels();
    }

    @Override
    public D saveConfig(final R restModel) throws AlertException {
        if (restModel != null) {
            try {
                D createdEntity = (D) getDatabaseContentConverter().populateDatabaseEntityFromRestModel(restModel);
                CommonDistributionConfigEntity commonEntity = (CommonDistributionConfigEntity) commonDistributionContentConverter.populateDatabaseEntityFromRestModel(restModel);
                if (createdEntity != null && commonEntity != null) {
                    createdEntity = getRepository().save(createdEntity);
                    commonEntity.setDistributionConfigId(createdEntity.getId());
                    commonEntity = commonDistributionRepository.save(commonEntity);
                    configuredProjectsActions.saveConfiguredProjects(commonEntity.getId(), restModel.getConfiguredProjects());
                    notificationTypesActions.saveNotificationTypes(commonEntity.getId(), restModel.getNotificationTypes());
                    cleanUpStaleChannelConfigurations();
                    return createdEntity;
                }
            } catch (final Exception e) {
                throw new AlertException(e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public D saveNewConfigUpdateFromSavedConfig(final R restModel) throws AlertException {
        return saveConfig(restModel);
    }

    @Override
    public void deleteConfig(final Long id) {
        if (id != null) {
            final Optional<CommonDistributionConfigEntity> commonEntity = commonDistributionRepository.findById(id);
            if (commonEntity.isPresent()) {
                final Long distributionConfigId = commonEntity.get().getDistributionConfigId();
                getRepository().deleteById(distributionConfigId);
                commonDistributionRepository.deleteById(id);
            }
            configuredProjectsActions.cleanUpConfiguredProjects();
            notificationTypesActions.removeOldNotificationTypes(id);
        }
    }

    public abstract void validateDistributionConfig(R restModel, Map<String, String> fieldErrors) throws AlertFieldException;

    @Override
    public String validateConfig(final R restModel) throws AlertFieldException {
        final Map<String, String> fieldErrors = new HashMap<>();
        if (StringUtils.isNotBlank(restModel.getName())) {
            final CommonDistributionConfigEntity entity = commonDistributionRepository.findByName(restModel.getName());
            if (entity != null && (entity.getId() != commonDistributionContentConverter.getContentConverter().getLongValue(restModel.getId()))) {
                fieldErrors.put("name", "A distribution configuration with this name already exists.");
            }
        } else {
            fieldErrors.put("name", "Name cannot be blank.");
        }
        if (StringUtils.isNotBlank(restModel.getId()) && !StringUtils.isNumeric(restModel.getId())) {
            fieldErrors.put("id", "Not an Integer.");
        }
        if (StringUtils.isNotBlank(restModel.getDistributionConfigId()) && !StringUtils.isNumeric(restModel.getDistributionConfigId())) {
            fieldErrors.put("distributionConfigId", "Not an Integer.");
        }
        if (StringUtils.isNotBlank(restModel.getFilterByProject()) && !isBoolean(restModel.getFilterByProject())) {
            fieldErrors.put("filterByProject", "Not a Boolean.");
        }
        if (StringUtils.isBlank(restModel.getFrequency())) {
            fieldErrors.put("frequency", "Frequency cannot be blank.");
        }
        if (restModel.getNotificationTypes() == null || restModel.getNotificationTypes().size() <= 0) {
            fieldErrors.put("notificationTypes", "Must have at least one notification type.");
        }
        validateDistributionConfig(restModel, fieldErrors);
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    private void cleanUpStaleChannelConfigurations() {
        final String distributionName = getDistributionName();
        if (distributionName != null) {
            final List<D> channelDistributionConfigEntities = getRepository().findAll();
            channelDistributionConfigEntities.forEach(entity -> {
                final CommonDistributionConfigEntity commonEntity = commonDistributionRepository.findByDistributionConfigIdAndDistributionType(entity.getId(), distributionName);
                if (commonEntity == null) {
                    getRepository().delete(entity);
                }
            });
        }
    }

    public List<R> constructRestModels() {
        final List<D> allEntities = getRepository().findAll();
        final List<R> constructedRestModels = new ArrayList<>();
        for (final D entity : allEntities) {
            try {
                final R restModel = constructRestModel(entity);
                if (restModel != null) {
                    constructedRestModels.add(restModel);
                } else {
                    logger.warn("Entity did not exist");
                }
            } catch (final AlertException e) {
                logger.warn("Problem constructing rest model", e);
            }
        }
        return constructedRestModels;
    }

    public R constructRestModel(final D entity) throws AlertException {
        final Optional<D> distributionEntity = getRepository().findById(entity.getId());
        final CommonDistributionConfigEntity commonEntity = commonDistributionRepository.findByDistributionConfigIdAndDistributionType(entity.getId(), getDistributionName());
        if (distributionEntity.isPresent() && commonEntity != null) {
            final R restModel = constructRestModel(commonEntity, distributionEntity.get());
            restModel.setConfiguredProjects(configuredProjectsActions.getConfiguredProjects(commonEntity));
            restModel.setNotificationTypes(notificationTypesActions.getNotificationTypes(commonEntity));
            return restModel;
        }
        return null;
    }

    @Override
    public boolean doesConfigExist(final Long id) {
        return id != null && commonDistributionRepository.existsById(id);
    }

    public abstract String getDistributionName();

    public abstract R constructRestModel(final CommonDistributionConfigEntity commonEntity, D distributionEntity) throws AlertException;

    public CommonDistributionRepository getCommonDistributionRepository() {
        return commonDistributionRepository;
    }

    public ConfiguredProjectsActions getConfiguredProjectsActions() {
        return configuredProjectsActions;
    }

    public NotificationTypesActions getNotificationTypesActions() {
        return notificationTypesActions;
    }

}
