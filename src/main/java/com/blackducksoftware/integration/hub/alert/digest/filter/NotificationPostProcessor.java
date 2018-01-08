/**
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.alert.digest.filter;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.ConfiguredProjectEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationTypeEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.ConfiguredProjectsRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.NotificationTypeRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.relation.DistributionNotificationTypeRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.DistributionProjectRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionNotificationTypeRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionProjectRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.digest.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;

@Transactional
@Component
public class NotificationPostProcessor {
    private final DistributionProjectRepositoryWrapper distributionProjectRepository;
    private final ConfiguredProjectsRepositoryWrapper configuredProjectsRepository;
    private final DistributionNotificationTypeRepositoryWrapper distributionNotificationTypeRepository;
    private final NotificationTypeRepositoryWrapper notificationTypeRepository;

    @Autowired
    public NotificationPostProcessor(final DistributionProjectRepositoryWrapper distributionProjectRepository, final ConfiguredProjectsRepositoryWrapper configuredProjectsRepository,
            final DistributionNotificationTypeRepositoryWrapper distributionNotificationTypeRepository, final NotificationTypeRepositoryWrapper notificationTypeRepository) {
        this.distributionProjectRepository = distributionProjectRepository;
        this.configuredProjectsRepository = configuredProjectsRepository;
        this.distributionNotificationTypeRepository = distributionNotificationTypeRepository;
        this.notificationTypeRepository = notificationTypeRepository;
    }

    public Set<CommonDistributionConfigEntity> getApplicableConfigurations(final Collection<CommonDistributionConfigEntity> distributionConfigurations, final ProjectData projectData) {
        final Set<CommonDistributionConfigEntity> applicableConfigurations = new HashSet<>();
        distributionConfigurations.forEach(distributionConfig -> {
            if (isApplicable(distributionConfig, projectData)) {
                if (distributionConfig.getFilterByProject()) {
                    final Set<CommonDistributionConfigEntity> filteredConfigurations = getApplicableConfigurationsFilteredByProject(distributionConfig, projectData);
                    applicableConfigurations.addAll(filteredConfigurations);
                } else {
                    applicableConfigurations.add(distributionConfig);
                }
            }
        });
        return applicableConfigurations;
    }

    public Set<CommonDistributionConfigEntity> getApplicableConfigurationsFilteredByProject(final CommonDistributionConfigEntity commonDistributionConfigEntity, final ProjectData projectData) {
        final Set<CommonDistributionConfigEntity> applicableConfigurations = new HashSet<>();
        final List<DistributionProjectRelation> foundRelations = distributionProjectRepository.findByCommonDistributionConfigId(commonDistributionConfigEntity.getId());
        foundRelations.forEach(relation -> {
            final ConfiguredProjectEntity foundEntity = configuredProjectsRepository.findOne(relation.getProjectId());
            if (foundEntity != null && foundEntity.getProjectName().equals(projectData.getProjectName())) {
                applicableConfigurations.add(commonDistributionConfigEntity);
            }
        });
        return applicableConfigurations;
    }

    public boolean isApplicable(final CommonDistributionConfigEntity commonDistributionConfigEntity, final ProjectData projectData) {
        return doFrequenciesMatch(commonDistributionConfigEntity, projectData) && doNotificationTypesMatch(commonDistributionConfigEntity, projectData);
    }

    public boolean doFrequenciesMatch(final CommonDistributionConfigEntity commonDistributionConfigEntity, final ProjectData projectData) {
        final DigestTypeEnum digestTypeEnum = projectData.getDigestType();
        final String digestName = digestTypeEnum.name();
        if (StringUtils.isNotBlank(commonDistributionConfigEntity.getFrequency())) {
            return commonDistributionConfigEntity.getFrequency().equals(digestName);
        }
        return false;
    }

    public boolean doNotificationTypesMatch(final CommonDistributionConfigEntity commonDistributionConfigEntity, final ProjectData projectData) {
        final List<DistributionNotificationTypeRelation> foundRelations = distributionNotificationTypeRepository.findByCommonDistributionConfigId(commonDistributionConfigEntity.getId());
        for (final DistributionNotificationTypeRelation foundRelation : foundRelations) {
            final NotificationTypeEntity foundEntity = notificationTypeRepository.findOne(foundRelation.getNotificationTypeId());
            for (final NotificationCategoryEnum category : projectData.getCategoryMap().keySet()) {
                if (category.toString().equalsIgnoreCase(foundEntity.getType())) {
                    return true;
                }
            }
        }
        return false;
    }

}
