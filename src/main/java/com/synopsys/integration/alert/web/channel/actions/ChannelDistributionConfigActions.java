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
package com.synopsys.integration.alert.web.channel.actions;

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

import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.enumeration.DescriptorConfigType;
import com.synopsys.integration.alert.common.enumeration.DigestType;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.audit.relation.AuditNotificationRelation;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.synopsys.integration.alert.web.actions.ConfiguredProjectsActions;
import com.synopsys.integration.alert.web.actions.NotificationTypesActions;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.alert.common.ContentConverter;

@Component
public class ChannelDistributionConfigActions extends ChannelConfigActions<CommonDistributionConfig> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final CommonDistributionRepository commonDistributionRepository;
    private final ConfiguredProjectsActions configuredProjectsActions;
    private final NotificationTypesActions notificationTypesActions;
    private final AuditEntryRepository auditEntryRepository;
    private final AuditNotificationRepository auditNotificationRepository;

    @Autowired
    public ChannelDistributionConfigActions(final CommonDistributionRepository commonDistributionRepository,
            final ConfiguredProjectsActions configuredProjectsActions, final NotificationTypesActions notificationTypesActions, final ContentConverter contentConverter,
            final AuditEntryRepository auditEntryRepository, final AuditNotificationRepository auditNotificationRepository) {
        super(contentConverter);
        this.commonDistributionRepository = commonDistributionRepository;
        this.configuredProjectsActions = configuredProjectsActions;
        this.notificationTypesActions = notificationTypesActions;
        this.auditEntryRepository = auditEntryRepository;
        this.auditNotificationRepository = auditNotificationRepository;
    }

    @Override
    public boolean doesConfigExist(final Long id, final ChannelDescriptor descriptor) {
        return id != null && commonDistributionRepository.existsById(id);
    }

    @Override
    public List<CommonDistributionConfig> getConfig(final Long id, final ChannelDescriptor descriptor) throws AlertException {
        cleanUpStaleChannelConfigurations(descriptor);
        if (id != null) {
            final Optional<? extends DatabaseEntity> foundEntity = descriptor.readEntity(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG, id);
            if (foundEntity.isPresent()) {
                return Arrays.asList(constructRestModel(foundEntity.get(), descriptor));
            }
            return Collections.emptyList();
        }
        final List<CommonDistributionConfig> restModels = constructRestModels(descriptor);
        addAuditEntryInfoToRestModels(restModels);
        return restModels;
    }

    public List<CommonDistributionConfig> constructRestModels(final ChannelDescriptor descriptor) {
        final List<? extends DatabaseEntity> allEntities = descriptor.readEntities(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG);
        final List<CommonDistributionConfig> constructedRestModels = new ArrayList<>();
        for (final DatabaseEntity entity : allEntities) {
            try {
                final CommonDistributionConfig restModel = constructRestModel(entity, descriptor);
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

    public CommonDistributionConfig constructRestModel(final DatabaseEntity entity, final ChannelDescriptor descriptor) throws AlertException {
        final CommonDistributionConfigEntity commonEntity = commonDistributionRepository.findByDistributionConfigIdAndDistributionType(entity.getId(), descriptor.getName());
        if (commonEntity != null) {
            final CommonDistributionConfig restModel = (CommonDistributionConfig) descriptor.populateConfigFromEntity(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG, entity);
            restModel.setId(getContentConverter().getStringValue(commonEntity.getId()));
            restModel.setDistributionConfigId(getContentConverter().getStringValue(entity.getId()));
            restModel.setDistributionType(commonEntity.getDistributionType());
            restModel.setFilterByProject(getContentConverter().getStringValue(commonEntity.getFilterByProject()));
            restModel.setFrequency(commonEntity.getFrequency().name());
            restModel.setName(commonEntity.getName());
            restModel.setConfiguredProjects(configuredProjectsActions.getConfiguredProjects(commonEntity));
            restModel.setNotificationTypes(notificationTypesActions.getNotificationTypes(commonEntity));
            return restModel;
        }
        return (CommonDistributionConfig) descriptor.populateConfigFromEntity(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG, entity);
    }

    private void addAuditEntryInfoToRestModels(final List<CommonDistributionConfig> restModels) {
        for (final CommonDistributionConfig restModel : restModels) {
            addAuditEntryInfoToRestModel(restModel);
        }
    }

    private void addAuditEntryInfoToRestModel(final CommonDistributionConfig restModel) {
        String lastRan = "Unknown";
        String status = "Unknown";
        final Long id = getContentConverter().getLongValue(restModel.getId());
        final AuditEntryEntity lastRanEntry = auditEntryRepository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(id);
        if (lastRanEntry != null) {
            lastRan = getContentConverter().getStringValue(lastRanEntry.getTimeLastSent());
            status = lastRanEntry.getStatus().getDisplayName();
        }
        restModel.setLastRan(lastRan);
        restModel.setStatus(status);
    }

    // TODO add a wrapper class for common and job configs.
    @Override
    public DatabaseEntity saveConfig(final CommonDistributionConfig restModel, final ChannelDescriptor descriptor) throws AlertException {
        if (restModel != null) {
            try {
                final DatabaseEntity createdEntity = descriptor.populateEntityFromConfig(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG, restModel);
                CommonDistributionConfigEntity commonEntity = createCommonEntity(restModel);
                if (createdEntity != null && commonEntity != null) {
                    final DatabaseEntity savedEntity = descriptor.saveEntity(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG, createdEntity);
                    commonEntity.setDistributionConfigId(savedEntity.getId());
                    commonEntity = commonDistributionRepository.save(commonEntity);
                    if (Boolean.TRUE.equals(commonEntity.getFilterByProject())) {
                        configuredProjectsActions.saveConfiguredProjects(commonEntity.getId(), restModel.getConfiguredProjects());
                    }
                    notificationTypesActions.saveNotificationTypes(commonEntity.getId(), restModel.getNotificationTypes());
                    return savedEntity;
                }
            } catch (final Exception e) {
                throw new AlertException(e.getMessage(), e);
            }
        }
        return null;

    }

    public CommonDistributionConfigEntity createCommonEntity(final CommonDistributionConfig restModel) {
        final Long distributionConfigId = getContentConverter().getLongValue(restModel.getDistributionConfigId());
        final DigestType digestType = Enum.valueOf(DigestType.class, restModel.getFrequency());
        final Boolean filterByProject = getContentConverter().getBooleanValue(restModel.getFilterByProject());
        final CommonDistributionConfigEntity commonEntity = new CommonDistributionConfigEntity(distributionConfigId, restModel.getDistributionType(), restModel.getName(), digestType, filterByProject);
        final Long longId = getContentConverter().getLongValue(restModel.getId());
        commonEntity.setId(longId);
        return commonEntity;
    }

    @Override
    public void deleteConfig(final Long id, final ChannelDescriptor descriptor) {
        if (id != null) {
            final Optional<CommonDistributionConfigEntity> commonEntity = commonDistributionRepository.findById(id);
            if (commonEntity.isPresent()) {
                final Long configId = commonEntity.get().getDistributionConfigId();
                deleteAuditEntries(id);
                commonDistributionRepository.deleteById(id);
                configuredProjectsActions.cleanUpConfiguredProjects();
                notificationTypesActions.removeOldNotificationTypes(id);
                descriptor.deleteEntity(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG, configId);
            }
        }
    }

    private void deleteAuditEntries(final Long configID) {
        final List<AuditEntryEntity> auditEntryList = auditEntryRepository.findByCommonConfigId(configID);
        auditEntryList.forEach((auditEntry) -> {
            final List<AuditNotificationRelation> relationList = auditNotificationRepository.findByAuditEntryId(auditEntry.getId());
            auditNotificationRepository.deleteAll(relationList);
        });
        auditEntryRepository.deleteAll(auditEntryList);
    }

    @Override
    public DatabaseEntity saveNewConfigUpdateFromSavedConfig(final CommonDistributionConfig restModel, final ChannelDescriptor descriptor) throws AlertException {
        return saveConfig(restModel, descriptor);
    }

    @Override
    public String validateConfig(final CommonDistributionConfig restModel, final ChannelDescriptor descriptor) throws AlertFieldException {
        final Map<String, String> fieldErrors = new HashMap<>();
        if (StringUtils.isNotBlank(restModel.getName())) {
            final CommonDistributionConfigEntity entity = commonDistributionRepository.findByName(restModel.getName());
            if (entity != null && (entity.getId() != getContentConverter().getLongValue(restModel.getId()))) {
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
        descriptor.validateConfig(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG, restModel, fieldErrors);
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    @Override
    public String testConfig(final CommonDistributionConfig restModel, final ChannelDescriptor descriptor) throws IntegrationException {
        // TODO second expression is a current workaround to allow Slack to have a global config to display in UI and still let tests work properly.
        if (descriptor.getConfig(DescriptorConfigType.CHANNEL_GLOBAL_CONFIG) != null && (descriptor.getConfig(DescriptorConfigType.CHANNEL_GLOBAL_CONFIG).getRepositoryAccessor() != null)) {
            if (descriptor.readEntities(DescriptorConfigType.CHANNEL_GLOBAL_CONFIG).isEmpty()) {
                return "ERROR: Missing global configuration!";
            }
        }

        final DatabaseEntity entity = descriptor.populateEntityFromConfig(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG, restModel);
        descriptor.testConfig(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG, entity);
        return "Succesfully sent test message.";
    }

    // TODO remove this in the future as we will want to clean up stale channels for a version before removing this method
    private void cleanUpStaleChannelConfigurations(final ChannelDescriptor descriptor) {
        final String distributionName = descriptor.getName();
        if (distributionName != null) {
            final List<? extends DatabaseEntity> channelDistributionConfigEntities = descriptor.readEntities(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG);
            channelDistributionConfigEntities.forEach(entity -> {
                final CommonDistributionConfigEntity commonEntity = commonDistributionRepository.findByDistributionConfigIdAndDistributionType(entity.getId(), distributionName);
                if (commonEntity == null) {
                    descriptor.deleteEntity(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG, entity.getId());
                }
            });
        }
    }

}
