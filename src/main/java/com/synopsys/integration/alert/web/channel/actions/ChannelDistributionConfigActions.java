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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.config.DescriptorConfig;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.audit.relation.AuditNotificationRelation;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.synopsys.integration.alert.web.actions.ConfigActions;
import com.synopsys.integration.alert.web.actions.DefaultConfigActions;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class ChannelDistributionConfigActions extends ConfigActions {
    private final CommonDistributionRepository commonDistributionRepository;
    private final AuditEntryRepository auditEntryRepository;
    private final AuditNotificationRepository auditNotificationRepository;
    private final CommonDistributionConfigHelper commonDistributionConfigHelper;

    @Autowired
    public ChannelDistributionConfigActions(final CommonDistributionRepository commonDistributionRepository, final ContentConverter contentConverter, final AuditEntryRepository auditEntryRepository,
            final AuditNotificationRepository auditNotificationRepository, final CommonDistributionConfigHelper commonDistributionConfigHelper, final DefaultConfigActions defaultConfigActions) {
        super(contentConverter, defaultConfigActions);
        this.commonDistributionRepository = commonDistributionRepository;
        this.auditEntryRepository = auditEntryRepository;
        this.auditNotificationRepository = auditNotificationRepository;
        this.commonDistributionConfigHelper = commonDistributionConfigHelper;
    }

    @Override
    public boolean doesConfigExist(final Long id, final DescriptorConfig descriptor) {
        return id != null && commonDistributionRepository.existsById(id);
    }

    @Override
    public List<? extends Config> getConfig(final Long id, final DescriptorConfig descriptor) throws AlertException {
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
    public DatabaseEntity saveConfig(final Config config, final DescriptorConfig descriptor) {
        if (config != null) {
            final CommonDistributionConfig commonConfig = (CommonDistributionConfig) config;
            final DatabaseEntity savedEntity = super.saveConfig(commonConfig, descriptor);
            commonDistributionConfigHelper.saveCommonEntity(commonConfig, savedEntity);
            return savedEntity;
        }
        return null;
    }

    @Override
    public void deleteConfig(final Long id, final DescriptorConfig descriptor) {
        if (id != null) {
            final Optional<CommonDistributionConfigEntity> commonEntity = commonDistributionRepository.findById(id);
            if (commonEntity.isPresent()) {
                deleteAuditEntries(id);
                commonDistributionConfigHelper.deleteCommonEntity(id);
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
    public DatabaseEntity updateConfig(final Config config, final DescriptorConfig descriptor) throws AlertException {
        return saveConfig(config, descriptor);
    }

    @Override
    public String validateConfig(final Config config, final DescriptorConfig descriptor) throws AlertFieldException {
        final Map<String, String> fieldErrors = new HashMap<>();
        final CommonDistributionConfig commonConfig = (CommonDistributionConfig) config;
        commonDistributionConfigHelper.validateCommonConfig(commonConfig, fieldErrors);
        return super.validateConfig(commonConfig, descriptor);
    }

    @Override
    public String testConfig(final Config config, final DescriptorConfig descriptor) throws IntegrationException {
        if (descriptor != null && descriptor.readEntities().isEmpty()) {
            return "ERROR: Missing global configuration!";

        }
        return super.testConfig(config, descriptor);
    }

    // TODO Move this method away from here and into the startup manager to clean up all orphans when Alert is rebooted
    // private void cleanUpStaleChannelConfigurations(final ChannelDescriptor descriptor) {
    // final String distributionName = descriptor.getName();
    // if (distributionName != null) {
    // final List<? extends DatabaseEntity> channelDistributionConfigEntities = descriptor.readEntities(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG);
    // channelDistributionConfigEntities.forEach(entity -> {
    // final CommonDistributionConfigEntity commonEntity = commonDistributionRepository.findByDistributionConfigIdAndDistributionType(entity.getId(), distributionName);
    // if (commonEntity == null) {
    // descriptor.deleteEntity(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG, entity.getId());
    // }
    // });
    // }
    // }

}
