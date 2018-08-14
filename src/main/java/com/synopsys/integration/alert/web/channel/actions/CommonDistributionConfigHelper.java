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

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.enumeration.DigestType;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.synopsys.integration.alert.web.actions.ConfiguredProjectsActions;
import com.synopsys.integration.alert.web.actions.NotificationTypesActions;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;

@Component
public class CommonDistributionConfigHelper {
    private final ConfiguredProjectsActions configuredProjectsActions;
    private final NotificationTypesActions notificationTypesActions;
    private final CommonDistributionRepository commonDistributionRepository;
    private final ContentConverter contentConverter;

    @Autowired
    public CommonDistributionConfigHelper(final ConfiguredProjectsActions configuredProjectsActions, final NotificationTypesActions notificationTypesActions, final CommonDistributionRepository commonDistributionRepository,
            final ContentConverter contentConverter) {
        this.configuredProjectsActions = configuredProjectsActions;
        this.notificationTypesActions = notificationTypesActions;
        this.commonDistributionRepository = commonDistributionRepository;
        this.contentConverter = contentConverter;
    }

    public Config populateCommonFieldsFromEntity(final CommonDistributionConfig channelConfig, final CommonDistributionConfigEntity commonEntity) {
        channelConfig.setId(contentConverter.getStringValue(commonEntity.getId()));
        channelConfig.setDistributionType(commonEntity.getDistributionType());
        channelConfig.setFilterByProject(contentConverter.getStringValue(commonEntity.getFilterByProject()));
        channelConfig.setFrequency(commonEntity.getFrequency().name());
        channelConfig.setName(commonEntity.getName());
        channelConfig.setConfiguredProjects(configuredProjectsActions.getConfiguredProjects(commonEntity));
        channelConfig.setNotificationTypes(notificationTypesActions.getNotificationTypes(commonEntity));
        return channelConfig;
    }

    public void validateCommonConfig(final CommonDistributionConfig commonConfig, final Map<String, String> fieldErrors) {
        if (StringUtils.isNotBlank(commonConfig.getName())) {
            final CommonDistributionConfigEntity entity = commonDistributionRepository.findByName(commonConfig.getName());
            if (entity != null && (entity.getId() != contentConverter.getLongValue(commonConfig.getId()))) {
                fieldErrors.put("name", "A distribution configuration with this name already exists.");
            }
        } else {
            fieldErrors.put("name", "Name cannot be blank.");
        }
        if (StringUtils.isNotBlank(commonConfig.getId()) && !StringUtils.isNumeric(commonConfig.getId())) {
            fieldErrors.put("id", "Not an Integer.");
        }
        if (StringUtils.isNotBlank(commonConfig.getDistributionConfigId()) && !StringUtils.isNumeric(commonConfig.getDistributionConfigId())) {
            fieldErrors.put("distributionConfigId", "Not an Integer.");
        }
        if (StringUtils.isNotBlank(commonConfig.getFilterByProject()) && !contentConverter.isBoolean(commonConfig.getFilterByProject())) {
            fieldErrors.put("filterByProject", "Not a Boolean.");
        }
        if (StringUtils.isBlank(commonConfig.getFrequency())) {
            fieldErrors.put("frequency", "Frequency cannot be blank.");
        }
        if (commonConfig.getNotificationTypes() == null || commonConfig.getNotificationTypes().size() <= 0) {
            fieldErrors.put("notificationTypes", "Must have at least one notification type.");
        }
    }

    public void deleteCommonEntity(final long id) {
        commonDistributionRepository.deleteById(id);
        configuredProjectsActions.cleanUpConfiguredProjects();
        notificationTypesActions.removeOldNotificationTypes(id);
    }

    public void saveCommonEntity(final CommonDistributionConfig commonChannelConfig, final DatabaseEntity savedChannelEntity) throws AlertException {
        if (commonChannelConfig != null) {
            try {
                CommonDistributionConfigEntity commonChannelEntity = createCommonEntity(commonChannelConfig);
                if (savedChannelEntity != null && commonChannelEntity != null) {
                    commonChannelEntity.setDistributionConfigId(savedChannelEntity.getId());
                    commonChannelEntity = commonDistributionRepository.save(commonChannelEntity);
                    if (Boolean.TRUE.equals(commonChannelEntity.getFilterByProject())) {
                        configuredProjectsActions.saveConfiguredProjects(commonChannelEntity.getId(), commonChannelConfig.getConfiguredProjects());
                    }
                    notificationTypesActions.saveNotificationTypes(commonChannelEntity.getId(), commonChannelConfig.getNotificationTypes());
                }
            } catch (final Exception e) {
                throw new AlertException(e.getMessage(), e);
            }
        }
    }

    public CommonDistributionConfigEntity createCommonEntity(final CommonDistributionConfig commonConfig) {
        final Long distributionConfigId = contentConverter.getLongValue(commonConfig.getDistributionConfigId());
        final DigestType digestType = Enum.valueOf(DigestType.class, commonConfig.getFrequency());
        final Boolean filterByProject = contentConverter.getBooleanValue(commonConfig.getFilterByProject());
        final CommonDistributionConfigEntity commonEntity = new CommonDistributionConfigEntity(distributionConfigId, commonConfig.getDistributionType(), commonConfig.getName(), digestType, filterByProject);
        final Long longId = contentConverter.getLongValue(commonConfig.getId());
        commonEntity.setId(longId);
        return commonEntity;
    }
}
