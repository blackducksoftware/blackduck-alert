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
package com.blackducksoftware.integration.hub.alert.web.actions.distribution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.alert.datasource.SimpleKeyRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.ConfigActions;
import com.blackducksoftware.integration.hub.alert.web.actions.ConfiguredProjectsActions;
import com.blackducksoftware.integration.hub.alert.web.actions.NotificationTypesActions;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

@Transactional
public abstract class DistributionConfigActions<D extends DatabaseEntity, R extends CommonDistributionConfigRestModel, W extends SimpleKeyRepositoryWrapper<D, ?>> extends ConfigActions<D, R, W> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CommonDistributionRepositoryWrapper commonDistributionRepository;
    private final ConfiguredProjectsActions<R> configuredProjectsActions;
    private final NotificationTypesActions<R> notificationTypesActions;

    public DistributionConfigActions(final Class<D> databaseEntityClass, final Class<R> configRestModelClass, final CommonDistributionRepositoryWrapper commonDistributionRepository, final W channelDistributionRepository,
            final ConfiguredProjectsActions<R> configuredProjectsActions, final NotificationTypesActions<R> notificationTypesActions, final ObjectTransformer objectTransformer) {
        super(databaseEntityClass, configRestModelClass, channelDistributionRepository, objectTransformer);
        this.commonDistributionRepository = commonDistributionRepository;
        this.configuredProjectsActions = configuredProjectsActions;
        this.notificationTypesActions = notificationTypesActions;
    }

    @Override
    public List<R> getConfig(final Long id) throws AlertException {
        if (id != null) {
            final D foundEntity = getRepository().findOne(id);
            if (foundEntity != null) {
                return Arrays.asList(constructRestModel(foundEntity));
            }
            return Collections.emptyList();
        }
        return constructRestModels();
    }

    @Override
    public D saveConfig(final R restModel) throws AlertException {
        if (restModel != null) {
            try {
                D createdEntity = getObjectTransformer().configRestModelToDatabaseEntity(restModel, getDatabaseEntityClass());
                CommonDistributionConfigEntity commonEntity = getObjectTransformer().configRestModelToDatabaseEntity(restModel, CommonDistributionConfigEntity.class);
                if (createdEntity != null && commonEntity != null) {
                    createdEntity = getRepository().save(createdEntity);
                    commonEntity.setDistributionConfigId(createdEntity.getId());
                    commonEntity = commonDistributionRepository.save(commonEntity);
                    configuredProjectsActions.saveConfiguredProjects(commonEntity, restModel);
                    notificationTypesActions.saveNotificationTypes(commonEntity, restModel);
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
            final CommonDistributionConfigEntity commonEntity = commonDistributionRepository.findOne(id);
            if (commonEntity != null) {
                final Long distributionConfigId = commonEntity.getDistributionConfigId();
                getRepository().delete(distributionConfigId);
                commonDistributionRepository.delete(id);
            }
            configuredProjectsActions.cleanUpConfiguredProjects();
            notificationTypesActions.removeOldNotificationTypes(id);
        }
    }

    @Override
    public String validateConfig(final R restModel) throws AlertFieldException {
        final Map<String, String> fieldErrors = new HashMap<>();
        if (restModel.getName() != null) {
            final List<CommonDistributionConfigEntity> configuredEntities = commonDistributionRepository.findAll();
            for (final CommonDistributionConfigEntity entity : configuredEntities) {
                final boolean areIdsEqual = entity.getId().toString().equals(restModel.getId());
                final boolean areNamesEqual = entity.getName().trim().equalsIgnoreCase((restModel.getName().trim()));
                if (!areIdsEqual && areNamesEqual) {
                    fieldErrors.put("name", "A distribution configuration with this name already exists.");
                    break;
                }
            }
        } else {
            fieldErrors.put("name", "Name cannot be null.");
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
        final D distributionEntity = getRepository().findOne(entity.getId());
        final CommonDistributionConfigEntity commonEntity = commonDistributionRepository.findByDistributionConfigIdAndDistributionType(entity.getId(), getDistributionName());
        if (distributionEntity != null && commonEntity != null) {
            final R restModel = constructRestModel(commonEntity, distributionEntity);
            restModel.setConfiguredProjects(configuredProjectsActions.getConfiguredProjects(commonEntity));
            restModel.setNotificationTypes(notificationTypesActions.getNotificationTypes(commonEntity));
            return restModel;
        }
        return null;
    }

    @Override
    public boolean doesConfigExist(final Long id) {
        return id != null && commonDistributionRepository.exists(id);
    }

    public abstract String getDistributionName();

    public abstract R constructRestModel(final CommonDistributionConfigEntity commonEntity, D distributionEntity) throws AlertException;

    public CommonDistributionRepositoryWrapper getCommonDistributionRepository() {
        return commonDistributionRepository;
    }

    public ConfiguredProjectsActions<R> getConfiguredProjectsActions() {
        return configuredProjectsActions;
    }

    public NotificationTypesActions<R> getNotificationTypesActions() {
        return notificationTypesActions;
    }

}
