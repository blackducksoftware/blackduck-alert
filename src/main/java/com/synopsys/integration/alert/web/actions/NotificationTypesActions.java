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
package com.synopsys.integration.alert.web.actions;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.repository.NotificationTypeRepository;
import com.synopsys.integration.alert.database.relation.DistributionNotificationTypeRelation;
import com.synopsys.integration.alert.database.relation.repository.DistributionNotificationTypeRepository;

@Component
public class NotificationTypesActions {
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

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<String> getNotificationTypes(final CommonDistributionConfigEntity commonEntity) {
        final List<DistributionNotificationTypeRelation> foundRelations = distributionNotificationTypeRepository.findByCommonDistributionConfigId(commonEntity.getId());
        return foundRelations.stream().map(DistributionNotificationTypeRelation::getNotificationType).collect(Collectors.toList());

    }

    @Transactional
    public void saveNotificationTypes(final long entityId, final List<String> configuredNotificationTypes) {
        if (configuredNotificationTypes != null) {
            removeOldNotificationTypes(entityId);
            addNewDistributionNotificationTypes(entityId, configuredNotificationTypes);
        } else {
            logger.warn("Configured notification types was null; notification types will not be updated.");
        }
    }

    @Transactional
    public void removeOldNotificationTypes(final Long commonDistributionConfigId) {
        final List<DistributionNotificationTypeRelation> distributionProjects = distributionNotificationTypeRepository.findByCommonDistributionConfigId(commonDistributionConfigId);
        distributionNotificationTypeRepository.deleteAll(distributionProjects);
    }

    private void addNewDistributionNotificationTypes(final Long commonDistributionConfigId, final List<String> notificationTypesFromRestModel) {
        notificationTypesFromRestModel.forEach(notificationType -> distributionNotificationTypeRepository.save(new DistributionNotificationTypeRelation(commonDistributionConfigId, notificationType)));
    }
}
