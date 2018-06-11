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
package com.blackducksoftware.integration.hub.alert.digest.filter;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.ConfiguredProjectEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationTypeEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.ConfiguredProjectsRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.NotificationTypeRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.DistributionNotificationTypeRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.DistributionProjectRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionNotificationTypeRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionProjectRepository;
import com.blackducksoftware.integration.hub.alert.enumeration.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;

@Transactional
@Component
public class NotificationPostProcessor {
    private final DistributionProjectRepository distributionProjectRepository;
    private final ConfiguredProjectsRepository configuredProjectsRepository;
    private final DistributionNotificationTypeRepository distributionNotificationTypeRepository;
    private final NotificationTypeRepository notificationTypeRepository;

    @Autowired
    public NotificationPostProcessor(final DistributionProjectRepository distributionProjectRepository, final ConfiguredProjectsRepository configuredProjectsRepository,
            final DistributionNotificationTypeRepository distributionNotificationTypeRepository, final NotificationTypeRepository notificationTypeRepository) {
        this.distributionProjectRepository = distributionProjectRepository;
        this.configuredProjectsRepository = configuredProjectsRepository;
        this.distributionNotificationTypeRepository = distributionNotificationTypeRepository;
        this.notificationTypeRepository = notificationTypeRepository;
    }

    public Set<CommonDistributionConfigEntity> getApplicableConfigurations(final Collection<CommonDistributionConfigEntity> distributionConfigurations, final NotificationModel notificationModel, final DigestTypeEnum digestTypeEnum) {
        final Set<CommonDistributionConfigEntity> applicableConfigurations = new HashSet<>();
        distributionConfigurations.forEach(distributionConfig -> {
            if (doFrequenciesMatch(distributionConfig, digestTypeEnum)) {
                if (distributionConfig.getFilterByProject()) {
                    final Set<CommonDistributionConfigEntity> filteredConfigurations = getApplicableConfigurationsFilteredByProject(distributionConfig, notificationModel);
                    applicableConfigurations.addAll(filteredConfigurations);
                } else {
                    applicableConfigurations.add(distributionConfig);
                }
            }
        });
        return applicableConfigurations;
    }

    public Set<CommonDistributionConfigEntity> getApplicableConfigurationsFilteredByProject(final CommonDistributionConfigEntity commonDistributionConfigEntity, final NotificationModel notificationModel) {
        final Set<CommonDistributionConfigEntity> applicableConfigurations = new HashSet<>();
        final List<DistributionProjectRelation> foundRelations = distributionProjectRepository.findByCommonDistributionConfigId(commonDistributionConfigEntity.getId());
        foundRelations.forEach(relation -> {
            final Optional<ConfiguredProjectEntity> foundEntity = configuredProjectsRepository.findById(relation.getProjectId());
            if (foundEntity.isPresent() && foundEntity.get().getProjectName().equals(notificationModel.getProjectName())) {
                applicableConfigurations.add(commonDistributionConfigEntity);
            }
        });
        return applicableConfigurations;
    }

    public boolean doFrequenciesMatch(final CommonDistributionConfigEntity commonDistributionConfigEntity, final DigestTypeEnum digestTypeEnum) {
        if (commonDistributionConfigEntity.getFrequency() != null) {
            return commonDistributionConfigEntity.getFrequency().equals(digestTypeEnum);
        }
        return false;
    }

    public Optional<NotificationModel> filterMatchingNotificationTypes(final CommonDistributionConfigEntity commonDistributionConfigEntity, final NotificationModel notificationModel) {
        final List<DistributionNotificationTypeRelation> foundRelations = distributionNotificationTypeRepository.findByCommonDistributionConfigId(commonDistributionConfigEntity.getId());
        for (final DistributionNotificationTypeRelation foundRelation : foundRelations) {
            final Optional<NotificationTypeEntity> foundEntity = notificationTypeRepository.findById(foundRelation.getNotificationTypeId());
            if (foundEntity.isPresent() && foundEntity.get().getType().equals(notificationModel.getNotificationType())) {
                return Optional.of(notificationModel);
            }
        }

        return Optional.empty();
    }

}
