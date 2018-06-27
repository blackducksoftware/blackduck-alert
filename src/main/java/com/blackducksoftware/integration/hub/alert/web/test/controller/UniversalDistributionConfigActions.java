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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.channel.manager.DistributionChannelManager;
import com.blackducksoftware.integration.hub.alert.channel.slack.SlackChannel;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.hub.alert.descriptor.ChannelDescriptor;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.ConfiguredProjectsActions;
import com.blackducksoftware.integration.hub.alert.web.actions.NotificationTypesActions;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

@Component
public class UniversalDistributionConfigActions {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ObjectTransformer objectTransformer;
    private final CommonDistributionRepository commonDistributionRepository;
    private final ConfiguredProjectsActions<CommonDistributionConfigRestModel> configuredProjectsActions;
    private final NotificationTypesActions<CommonDistributionConfigRestModel> notificationTypesActions;
    private final DistributionChannelManager distributionChannelManager;
    private final UniversalConfigActions<CommonDistributionConfigRestModel> configActions;

    @Autowired
    public UniversalDistributionConfigActions(final ObjectTransformer objectTransformer, final CommonDistributionRepository commonDistributionRepository,
            final ConfiguredProjectsActions<CommonDistributionConfigRestModel> configuredProjectsActions, final NotificationTypesActions<CommonDistributionConfigRestModel> notificationTypesActions,
            final DistributionChannelManager distributionChannelManager, final UniversalConfigActions<CommonDistributionConfigRestModel> configActions) {
        this.objectTransformer = objectTransformer;
        this.commonDistributionRepository = commonDistributionRepository;
        this.configuredProjectsActions = configuredProjectsActions;
        this.notificationTypesActions = notificationTypesActions;
        this.distributionChannelManager = distributionChannelManager;
        this.configActions = configActions;
    }

    public List<CommonDistributionConfigRestModel> getConfig(final Long id, final ChannelDescriptor descriptor) throws AlertException {
        if (id != null) {
            final Optional<DatabaseEntity> foundEntity = descriptor.getDistributionRepository().findById(id);
            if (foundEntity.isPresent()) {
                return Arrays.asList(constructRestModel(foundEntity.get(), descriptor));
            }
            return Collections.emptyList();
        }
        return constructRestModels(descriptor);
    }

    public DatabaseEntity saveConfig(final CommonDistributionConfigRestModel restModel, final ChannelDescriptor descriptor) throws AlertException {
        if (restModel != null) {
            try {
                DatabaseEntity createdEntity = objectTransformer.configRestModelToDatabaseEntity(restModel, descriptor.getDistributionEntityClass());
                CommonDistributionConfigEntity commonEntity = objectTransformer.configRestModelToDatabaseEntity(restModel, CommonDistributionConfigEntity.class);
                if (createdEntity != null && commonEntity != null) {
                    createdEntity = descriptor.getDistributionRepository().save(createdEntity);
                    commonEntity.setDistributionConfigId(createdEntity.getId());
                    commonEntity = commonDistributionRepository.save(commonEntity);
                    configuredProjectsActions.saveConfiguredProjects(commonEntity, restModel);
                    notificationTypesActions.saveNotificationTypes(commonEntity, restModel);
                    cleanUpStaleChannelConfigurations(descriptor);
                    return createdEntity;
                }
            } catch (final Exception e) {
                throw new AlertException(e.getMessage(), e);
            }
        }
        return null;
    }

    public DatabaseEntity saveNewConfigUpdateFromSavedConfig(final CommonDistributionConfigRestModel restModel, final ChannelDescriptor descriptor) throws AlertException {
        return saveConfig(restModel, descriptor);
    }

    public void deleteConfig(final String id, final ChannelDescriptor descriptor) {
        deleteConfig(objectTransformer.stringToLong(id), descriptor);
    }

    public void deleteConfig(final Long id, final ChannelDescriptor descriptor) {
        if (id != null) {
            final Optional<CommonDistributionConfigEntity> commonEntity = commonDistributionRepository.findById(id);
            if (commonEntity.isPresent()) {
                final Long distributionConfigId = commonEntity.get().getDistributionConfigId();
                descriptor.getDistributionRepository().deleteById(distributionConfigId);
                commonDistributionRepository.deleteById(id);
            }
            configuredProjectsActions.cleanUpConfiguredProjects();
            notificationTypesActions.removeOldNotificationTypes(id);
        }
    }

    public String validateConfig(final CommonDistributionConfigRestModel restModel, final ChannelDescriptor descriptor) throws AlertFieldException {
        final Map<String, String> fieldErrors = new HashMap<>();
        if (StringUtils.isNotBlank(restModel.getName())) {
            final CommonDistributionConfigEntity entity = commonDistributionRepository.findByName(restModel.getName());
            if (entity != null && (entity.getId() != objectTransformer.stringToLong(restModel.getId()))) {
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
        if (StringUtils.isNotBlank(restModel.getFilterByProject()) && !configActions.isBoolean(restModel.getFilterByProject())) {
            fieldErrors.put("filterByProject", "Not a Boolean.");
        }
        if (StringUtils.isBlank(restModel.getFrequency())) {
            fieldErrors.put("frequency", "Frequency cannot be blank.");
        }
        if (restModel.getNotificationTypes() == null || restModel.getNotificationTypes().size() <= 0) {
            fieldErrors.put("notificationTypes", "Must have at least one notification type.");
        }
        descriptor.getDistributionConfigActions().validateConfig(restModel, fieldErrors);
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    public String testConfig(final CommonDistributionConfigRestModel restModel, final ChannelDescriptor descriptor) throws IntegrationException {
        if (restModel != null && StringUtils.isNotBlank(restModel.getId())) {
            configActions.fillNewConfigWithSavedConfig(restModel, descriptor.getDistributionRepository(), descriptor.getDistributionEntityClass());
        }
        return distributionChannelManager.sendTestMessage(SlackChannel.COMPONENT_NAME, restModel);
    }

    private void cleanUpStaleChannelConfigurations(final ChannelDescriptor descriptor) {
        final String distributionName = descriptor.getName();
        if (distributionName != null) {
            final List<DatabaseEntity> channelDistributionConfigEntities = descriptor.getDistributionRepository().findAll();
            channelDistributionConfigEntities.forEach(entity -> {
                final CommonDistributionConfigEntity commonEntity = commonDistributionRepository.findByDistributionConfigIdAndDistributionType(entity.getId(), distributionName);
                if (commonEntity == null) {
                    descriptor.getDistributionRepository().delete(entity);
                }
            });
        }
    }

    public List<CommonDistributionConfigRestModel> constructRestModels(final ChannelDescriptor descriptor) {
        final List<DatabaseEntity> allEntities = descriptor.getDistributionRepository().findAll();
        final List<CommonDistributionConfigRestModel> constructedRestModels = new ArrayList<>();
        for (final DatabaseEntity entity : allEntities) {
            try {
                final CommonDistributionConfigRestModel restModel = constructRestModel(entity, descriptor);
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

    public CommonDistributionConfigRestModel constructRestModel(final DatabaseEntity entity, final ChannelDescriptor descriptor) throws AlertException {
        final Optional<DatabaseEntity> distributionEntity = descriptor.getDistributionRepository().findById(entity.getId());
        final CommonDistributionConfigEntity commonEntity = commonDistributionRepository.findByDistributionConfigIdAndDistributionType(entity.getId(), descriptor.getName());
        if (distributionEntity.isPresent() && commonEntity != null) {
            final CommonDistributionConfigRestModel restModel = descriptor.getDistributionConfigActions().constructRestModel(commonEntity, distributionEntity.get());
            restModel.setConfiguredProjects(configuredProjectsActions.getConfiguredProjects(commonEntity));
            restModel.setNotificationTypes(notificationTypesActions.getNotificationTypes(commonEntity));
            return restModel;
        }
        return null;
    }

    public UniversalConfigActions<CommonDistributionConfigRestModel> getConfigActions() {
        return configActions;
    }

}
