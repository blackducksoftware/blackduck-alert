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

import com.blackducksoftware.integration.alert.channel.DistributionChannelManager;
import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.descriptor.ChannelDescriptor;
import com.blackducksoftware.integration.alert.common.exception.AlertException;
import com.blackducksoftware.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.alert.web.actions.ConfiguredProjectsActions;
import com.blackducksoftware.integration.alert.web.actions.NotificationTypesActions;
import com.blackducksoftware.integration.alert.web.exception.AlertFieldException;
import com.blackducksoftware.integration.alert.web.model.CommonDistributionConfigRestModel;
import com.blackducksoftware.integration.alert.web.model.CommonDistributionContentConverter;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.exception.IntegrationException;

@Component
public class ChannelDistributionConfigActions extends ChannelConfigActions<CommonDistributionConfigRestModel> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final CommonDistributionRepository commonDistributionRepository;
    private final ConfiguredProjectsActions configuredProjectsActions;
    private final NotificationTypesActions notificationTypesActions;
    private final DistributionChannelManager distributionChannelManager;
    private final CommonDistributionContentConverter commonDistributionContentConverter;
    private final ContentConverter contentConverter;

    @Autowired
    public ChannelDistributionConfigActions(final CommonDistributionRepository commonDistributionRepository,
            final ConfiguredProjectsActions configuredProjectsActions, final NotificationTypesActions notificationTypesActions,
            final DistributionChannelManager distributionChannelManager, final CommonDistributionContentConverter commonDistributionContentConverter, final ContentConverter contentConverter) {
        super(contentConverter);
        this.commonDistributionRepository = commonDistributionRepository;
        this.configuredProjectsActions = configuredProjectsActions;
        this.notificationTypesActions = notificationTypesActions;
        this.distributionChannelManager = distributionChannelManager;
        this.commonDistributionContentConverter = commonDistributionContentConverter;
        this.contentConverter = contentConverter;
    }

    @Override
    public boolean doesConfigExist(final Long id, final ChannelDescriptor descriptor) {
        return id != null && commonDistributionRepository.existsById(id);
    }

    @Override
    public List<ConfigRestModel> getConfig(final Long id, final ChannelDescriptor descriptor) throws AlertException {
        if (id != null) {
            final Optional<? extends DatabaseEntity> foundEntity = descriptor.getDistributionRepositoryAccessor().readEntity(id);
            if (foundEntity.isPresent()) {
                return Arrays.asList(constructRestModel(foundEntity.get(), descriptor));
            }
            return Collections.emptyList();
        }
        return constructRestModels(descriptor);
    }

    @Override
    public DatabaseEntity saveConfig(final CommonDistributionConfigRestModel restModel, final ChannelDescriptor descriptor) throws AlertException {
        if (restModel != null) {
            try {
                final DatabaseEntity createdEntity = descriptor.getDistributionContentConverter().populateDatabaseEntityFromRestModel(restModel);
                CommonDistributionConfigEntity commonEntity = (CommonDistributionConfigEntity) commonDistributionContentConverter.populateDatabaseEntityFromRestModel(restModel);
                if (createdEntity != null && commonEntity != null) {
                    final DatabaseEntity savedEntity = descriptor.getDistributionRepositoryAccessor().saveEntity(createdEntity);
                    commonEntity.setDistributionConfigId(savedEntity.getId());
                    commonEntity = commonDistributionRepository.save(commonEntity);
                    if (Boolean.TRUE.equals(commonEntity.getFilterByProject())) {
                        configuredProjectsActions.saveConfiguredProjects(commonEntity.getId(), restModel.getConfiguredProjects());
                    }
                    notificationTypesActions.saveNotificationTypes(commonEntity.getId(), restModel.getNotificationTypes());
                    cleanUpStaleChannelConfigurations(descriptor);
                    return savedEntity;
                }
            } catch (final Exception e) {
                throw new AlertException(e.getMessage(), e);
            }
        }
        return null;

    }

    @Override
    public void deleteConfig(final Long id, final ChannelDescriptor descriptor) {
        if (id != null) {
            final Optional<CommonDistributionConfigEntity> commonEntity = commonDistributionRepository.findById(id);
            if (commonEntity.isPresent()) {
                final Long distributionConfigId = commonEntity.get().getDistributionConfigId();
                descriptor.getDistributionRepositoryAccessor().deleteEntity(distributionConfigId);
                commonDistributionRepository.deleteById(id);
            }
            configuredProjectsActions.cleanUpConfiguredProjects();
            notificationTypesActions.removeOldNotificationTypes(id);
        }
    }

    @Override
    public DatabaseEntity saveNewConfigUpdateFromSavedConfig(final CommonDistributionConfigRestModel restModel, final ChannelDescriptor descriptor) throws AlertException {
        return saveConfig(restModel, descriptor);
    }

    @Override
    public String validateConfig(final CommonDistributionConfigRestModel restModel, final ChannelDescriptor descriptor) throws AlertFieldException {
        final Map<String, String> fieldErrors = new HashMap<>();
        if (StringUtils.isNotBlank(restModel.getName())) {
            final CommonDistributionConfigEntity entity = commonDistributionRepository.findByName(restModel.getName());
            if (entity != null && (entity.getId() != contentConverter.getLongValue(restModel.getId()))) {
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
        descriptor.validateDistributionConfig(restModel, fieldErrors);
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    @Override
    public String testConfig(final CommonDistributionConfigRestModel restModel, final ChannelDescriptor descriptor) throws IntegrationException {
        return distributionChannelManager.sendTestMessage(restModel, descriptor);
    }

    private void cleanUpStaleChannelConfigurations(final ChannelDescriptor descriptor) {
        final String distributionName = descriptor.getName();
        if (distributionName != null) {
            final List<? extends DatabaseEntity> channelDistributionConfigEntities = descriptor.getDistributionRepositoryAccessor().readEntities();
            channelDistributionConfigEntities.forEach(entity -> {
                final CommonDistributionConfigEntity commonEntity = commonDistributionRepository.findByDistributionConfigIdAndDistributionType(entity.getId(), distributionName);
                if (commonEntity == null) {
                    descriptor.getDistributionRepositoryAccessor().deleteEntity(entity.getId());
                }
            });
        }
    }

    public List<ConfigRestModel> constructRestModels(final ChannelDescriptor descriptor) {
        final List<? extends DatabaseEntity> allEntities = descriptor.getDistributionRepositoryAccessor().readEntities();
        final List<ConfigRestModel> constructedRestModels = new ArrayList<>();
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
        final CommonDistributionConfigEntity commonEntity = commonDistributionRepository.findByDistributionConfigIdAndDistributionType(entity.getId(), descriptor.getName());
        if (commonEntity != null) {
            final CommonDistributionConfigRestModel restModel = (CommonDistributionConfigRestModel) descriptor.getDistributionContentConverter().populateRestModelFromDatabaseEntity(entity);
            restModel.setId(String.valueOf(commonEntity.getId()));
            restModel.setDistributionConfigId(contentConverter.getStringValue(entity.getId()));
            restModel.setDistributionType(commonEntity.getDistributionType());
            restModel.setFilterByProject(contentConverter.getStringValue(commonEntity.getFilterByProject()));
            restModel.setFrequency(commonEntity.getFrequency().name());
            restModel.setName(commonEntity.getName());
            restModel.setConfiguredProjects(configuredProjectsActions.getConfiguredProjects(commonEntity));
            restModel.setNotificationTypes(notificationTypesActions.getNotificationTypes(commonEntity));
            return restModel;
        }
        return null;
    }

}
