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
package com.blackducksoftware.integration.hub.alert.web.actions.distribution;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.datasource.entity.AuditEntryEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.AuditEntryRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.ConfiguredProjectsActions;
import com.blackducksoftware.integration.hub.alert.web.actions.NotificationTypesActions;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

@Component
public class CommonDistributionConfigActions extends DistributionConfigActions<CommonDistributionConfigEntity, CommonDistributionConfigRestModel> {
    private final AuditEntryRepository auditEntryRepository;

    @Autowired
    public CommonDistributionConfigActions(final CommonDistributionRepository commonDistributionRepository, final AuditEntryRepository auditEntryRepository,
            final ConfiguredProjectsActions<CommonDistributionConfigRestModel> configuredProjectsActions, final NotificationTypesActions<CommonDistributionConfigRestModel> notificationTypesActions,
            final ObjectTransformer objectTransformer) {
        super(CommonDistributionConfigEntity.class, CommonDistributionConfigRestModel.class, commonDistributionRepository, commonDistributionRepository, configuredProjectsActions, notificationTypesActions, objectTransformer);
        this.auditEntryRepository = auditEntryRepository;
    }

    @Override
    public List<CommonDistributionConfigRestModel> getConfig(final Long id) throws AlertException {
        final List<CommonDistributionConfigRestModel> restModels = super.getConfig(id);
        addAuditEntryInfoToRestModels(restModels);
        return restModels;
    }

    private void addAuditEntryInfoToRestModels(final List<CommonDistributionConfigRestModel> restModels) {
        for (final CommonDistributionConfigRestModel restModel : restModels) {
            addAuditEntryInfoToRestModel(restModel);
        }
    }

    private void addAuditEntryInfoToRestModel(final CommonDistributionConfigRestModel restModel) {
        String lastRan = "Unknown";
        String status = "Unknown";
        final Long id = getObjectTransformer().stringToLong(restModel.getId());
        final AuditEntryEntity lastRanEntry = auditEntryRepository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(id);
        if (lastRanEntry != null) {
            lastRan = getObjectTransformer().objectToString(lastRanEntry.getTimeLastSent());
            status = lastRanEntry.getStatus().getDisplayName();
        }
        restModel.setLastRan(lastRan);
        restModel.setStatus(status);
    }

    @Override
    public CommonDistributionConfigEntity saveConfig(final CommonDistributionConfigRestModel restModel) throws AlertException {
        if (restModel != null) {
            try {
                CommonDistributionConfigEntity createdEntity = getObjectTransformer().configRestModelToDatabaseEntity(restModel, getDatabaseEntityClass());
                if (createdEntity != null) {
                    createdEntity = getCommonDistributionRepository().save(createdEntity);
                    getConfiguredProjectsActions().saveConfiguredProjects(createdEntity, restModel);
                    getNotificationTypesActions().saveNotificationTypes(createdEntity, restModel);
                    return createdEntity;
                }
            } catch (final Exception e) {
                throw new AlertException(e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public void deleteConfig(final Long id) {
        if (id != null) {
            getCommonDistributionRepository().delete(id);
            getConfiguredProjectsActions().cleanUpConfiguredProjects();
            getNotificationTypesActions().removeOldNotificationTypes(id);
        }
    }

    @Override
    public String channelTestConfig(final CommonDistributionConfigRestModel restModel) throws IntegrationException {
        // Should not be tested
        return "Configuration should not be tested at this level.";
    }

    @Override
    public CommonDistributionConfigRestModel constructRestModel(final CommonDistributionConfigEntity entity) throws AlertException {
        final CommonDistributionConfigEntity foundEntity = getCommonDistributionRepository().findOne(entity.getId());
        if (foundEntity != null) {
            return constructRestModel(foundEntity, null);
        }
        return null;
    }

    @Override
    public CommonDistributionConfigRestModel constructRestModel(final CommonDistributionConfigEntity commonEntity, final CommonDistributionConfigEntity distributionEntity) throws AlertException {
        final CommonDistributionConfigRestModel restModel = getObjectTransformer().databaseEntityToConfigRestModel(commonEntity, CommonDistributionConfigRestModel.class);
        restModel.setConfiguredProjects(getConfiguredProjectsActions().getConfiguredProjects(commonEntity));
        restModel.setNotificationTypes(getNotificationTypesActions().getNotificationTypes(commonEntity));
        return restModel;
    }

    @Override
    public String getDistributionName() {
        // This does not have a distribution name
        return null;
    }

}
