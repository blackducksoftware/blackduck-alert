/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.workflow.upgrade.step._4_0_0.conversion;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.ui.CommonDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.deprecated.channel.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.deprecated.channel.CommonDistributionRepository;
import com.synopsys.integration.alert.database.entity.repository.NotificationTypeRepository;
import com.synopsys.integration.alert.database.relation.repository.DistributionNotificationTypeRepository;

@Component
public class CommonDistributionFieldCreator {
    private final CommonDistributionRepository commonDistributionRepository;
    private final DistributionNotificationTypeRepository distributionNotificationTypeRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final FieldCreatorUtil fieldCreatorUtil;

    public CommonDistributionFieldCreator(final CommonDistributionRepository commonDistributionRepository, final DistributionNotificationTypeRepository distributionNotificationTypeRepository,
        final NotificationTypeRepository notificationTypeRepository, final FieldCreatorUtil fieldCreatorUtil) {
        this.commonDistributionRepository = commonDistributionRepository;
        this.distributionNotificationTypeRepository = distributionNotificationTypeRepository;
        this.notificationTypeRepository = notificationTypeRepository;
        this.fieldCreatorUtil = fieldCreatorUtil;
    }

    public List<ConfigurationFieldModel> createCommonFields(final String descriptorName, final Long configId) {
        final CommonDistributionConfigEntity commonDistributionEntity = commonDistributionRepository.findByDistributionConfigIdAndDistributionType(configId, descriptorName);
        final List<ConfigurationFieldModel> configurationFieldModels = new LinkedList<>();

        final String name = commonDistributionEntity.getName();
        final String storedDescriptorName = commonDistributionEntity.getDistributionType();
        final FrequencyType frequency = commonDistributionEntity.getFrequency();
        final String providerName = commonDistributionEntity.getProviderName();

        fieldCreatorUtil.addFieldModel(CommonDistributionUIConfig.KEY_NAME, name, configurationFieldModels);
        fieldCreatorUtil.addFieldModel(CommonDistributionUIConfig.KEY_CHANNEL_NAME, storedDescriptorName, configurationFieldModels);
        fieldCreatorUtil.addFieldModel(CommonDistributionUIConfig.KEY_FREQUENCY, frequency, configurationFieldModels);
        fieldCreatorUtil.addFieldModel(CommonDistributionUIConfig.KEY_PROVIDER_NAME, providerName, configurationFieldModels);

        return configurationFieldModels;
    }

    /* FIXME We need to also take out the provider specific info from CommonDistributionConfigEntity and store it into the BD provider distribution configuration
        CommonProvider:
        formatType
        notificationType
        BlackDuckProvider:
        filterByProject
        projectNamePattern
    */
}
