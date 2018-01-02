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
package com.blackducksoftware.integration.hub.alert.web.actions;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationTypeEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.NotificationTypeRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.DistributionNotificationTypeRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionNotificationTypeRepository;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

@Component
public class NotificationTypesActions<R extends CommonDistributionConfigRestModel> {
    private static final Logger logger = LoggerFactory.getLogger(NotificationTypesActions.class);

    private final NotificationTypeRepository notificationTypeRepository;
    private final DistributionNotificationTypeRepository distributionNotificationTypeRepository;

    @Autowired
    public NotificationTypesActions(final NotificationTypeRepository notificationTypeRepository, final DistributionNotificationTypeRepository distributionNotificationTypeRepository) {
        this.notificationTypeRepository = notificationTypeRepository;
        this.distributionNotificationTypeRepository = distributionNotificationTypeRepository;
    }

    public NotificationTypeRepository getNotificationTypeRepository() {
        return notificationTypeRepository;
    }

    public DistributionNotificationTypeRepository getDistributionNotificationTypeRepository() {
        return distributionNotificationTypeRepository;
    }

    public List<String> getNotificationTypes(final CommonDistributionConfigEntity commonEntity) {
        final List<DistributionNotificationTypeRelation> foundRelations = distributionNotificationTypeRepository.findByCommonDistributionConfigId(commonEntity.getId());
        final List<String> notificationTypes = new ArrayList<>(foundRelations.size());
        for (final DistributionNotificationTypeRelation relation : foundRelations) {
            final NotificationTypeEntity foundEntity = notificationTypeRepository.findOne(relation.getNotificationTypeId());
            notificationTypes.add(foundEntity.getType());
        }
        return notificationTypes;
    }

    public void saveNotificationTypes(final CommonDistributionConfigEntity commonEntity, final R restModel) {
        final List<String> configuredProjectsFromRestModel = restModel.getNotificationTypes();
        if (configuredProjectsFromRestModel != null) {
            removeOldNotificationTypes(commonEntity.getId());
            addNewDistributionNotificationTypes(commonEntity.getId(), configuredProjectsFromRestModel);
        } else {
            logger.warn("{}: List of configured projects was null; configured projects will not be updated.", commonEntity.getName());
        }
    }

    public void removeOldNotificationTypes(final Long commonDistributionConfigId) {
        final List<DistributionNotificationTypeRelation> distributionProjects = distributionNotificationTypeRepository.findByCommonDistributionConfigId(commonDistributionConfigId);
        distributionNotificationTypeRepository.delete(distributionProjects);
    }

    private void addNewDistributionNotificationTypes(final Long commonDistributionConfigId, final List<String> notificationTypesFromRestModel) {
        for (final String notificationType : notificationTypesFromRestModel) {
            Long notificationTypeId;
            final NotificationTypeEntity foundEntity = notificationTypeRepository.findByType(notificationType);
            if (foundEntity != null) {
                notificationTypeId = foundEntity.getId();
            } else {
                final NotificationTypeEntity createdEntity = notificationTypeRepository.save(new NotificationTypeEntity(notificationType));
                notificationTypeId = createdEntity.getId();
            }
            distributionNotificationTypeRepository.save(new DistributionNotificationTypeRelation(commonDistributionConfigId, notificationTypeId));
        }
    }

}
