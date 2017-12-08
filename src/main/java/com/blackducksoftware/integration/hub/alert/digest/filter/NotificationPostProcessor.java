/**
 * Copyright (C) 2017 Black Duck Software, Inc.
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.ConfiguredProjectEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.ConfiguredProjectsRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.DistributionProjectRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionProjectRepository;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;

@Component
public class NotificationPostProcessor {
    private final DistributionProjectRepository distributionProjectRepository;
    private final ConfiguredProjectsRepository configuredProjectsRepository;

    @Autowired
    public NotificationPostProcessor(final DistributionProjectRepository distributionProjectRepository, final ConfiguredProjectsRepository configuredProjectsRepository) {
        this.distributionProjectRepository = distributionProjectRepository;
        this.configuredProjectsRepository = configuredProjectsRepository;
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
        return commonDistributionConfigEntity.getFrequency().equals(projectData.getDigestType().enumAsString());
    }

    public boolean doNotificationTypesMatch(final CommonDistributionConfigEntity commonDistributionConfigEntity, final ProjectData projectData) {
        final String notificationType = commonDistributionConfigEntity.getNotificationType();
        if ("ALL".equals(notificationType)) {
            return true;
        }
        for (final NotificationCategoryEnum category : projectData.getCategoryMap().keySet()) {
            if (category.toString().contains(notificationType)) {
                return true;
            }
        }
        return false;
    }

}
