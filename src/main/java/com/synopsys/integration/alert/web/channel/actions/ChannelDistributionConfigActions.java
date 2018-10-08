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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.config.DescriptorActionApi;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.audit.relation.AuditNotificationRelation;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.synopsys.integration.alert.web.config.actions.DescriptorConfigActions;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;

@Component
public class ChannelDistributionConfigActions extends DescriptorConfigActions {
    private final CommonDistributionRepository commonDistributionRepository;
    private final AuditEntryRepository auditEntryRepository;
    private final AuditNotificationRepository auditNotificationRepository;
    private final CommonDistributionConfigActions commonDistributionConfigActions;

    @Autowired
    public ChannelDistributionConfigActions(final CommonDistributionRepository commonDistributionRepository, final ContentConverter contentConverter, final AuditEntryRepository auditEntryRepository,
        final AuditNotificationRepository auditNotificationRepository, final CommonDistributionConfigActions commonDistributionConfigActions) {
        super(contentConverter);
        this.commonDistributionRepository = commonDistributionRepository;
        this.auditEntryRepository = auditEntryRepository;
        this.auditNotificationRepository = auditNotificationRepository;
        this.commonDistributionConfigActions = commonDistributionConfigActions;
    }

    @Override
    public boolean doesConfigExist(final Long id, final DescriptorActionApi descriptor) {
        return id != null && commonDistributionRepository.existsById(id);
    }

    @Override
    public List<? extends Config> getConfig(final Long id, final DescriptorActionApi descriptor) throws AlertException {
        final List<? extends Config> restModels = super.getConfig(id, descriptor);
        addAuditEntryInfoToRestModels(restModels);
        return restModels;
    }

    private void addAuditEntryInfoToRestModels(final List<? extends Config> configs) {
        for (final Config config : configs) {
            addAuditEntryInfoToRestModel((CommonDistributionConfig) config);
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

    @Override
    public DatabaseEntity saveConfig(final Config config, final DescriptorActionApi descriptor) {
        if (config != null) {
            final CommonDistributionConfig commonConfig = (CommonDistributionConfig) config;
            final DatabaseEntity savedChannelEntity = super.saveConfig(commonConfig, descriptor);
            commonDistributionConfigActions.saveCommonEntity(commonConfig, savedChannelEntity.getId());
            return savedChannelEntity;
        }
        return null;
    }

    @Override
    public void deleteConfig(final Long id, final DescriptorActionApi descriptor) {
        if (id != null) {
            final Optional<CommonDistributionConfigEntity> commonEntity = commonDistributionRepository.findById(id);
            if (commonEntity.isPresent()) {
                deleteAuditEntries(id);
                commonDistributionConfigActions.deleteCommonEntity(id);
                final Long configId = commonEntity.get().getDistributionConfigId();
                super.deleteConfig(configId, descriptor);
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
    public DatabaseEntity updateConfig(final Config config, final DescriptorActionApi descriptor) throws AlertException {
        return saveConfig(config, descriptor);
    }

    @Override
    public String validateConfig(final Config config, final DescriptorActionApi descriptor, Map<String, String> fieldErrors) throws AlertFieldException {
        final CommonDistributionConfig commonConfig = (CommonDistributionConfig) config;
        commonDistributionConfigActions.validateCommonConfig(commonConfig, fieldErrors);
        return super.validateConfig(commonConfig, descriptor, fieldErrors);
    }

}
