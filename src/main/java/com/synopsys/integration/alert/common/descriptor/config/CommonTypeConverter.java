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
package com.synopsys.integration.alert.common.descriptor.config;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.web.actions.ConfiguredProjectsActions;
import com.synopsys.integration.alert.web.actions.NotificationTypesActions;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;

@Component
@Transactional
public class CommonTypeConverter {
    private final ConfiguredProjectsActions configuredProjectsActions;
    private final NotificationTypesActions notificationTypesActions;
    private final ContentConverter contentConverter;

    @Autowired
    public CommonTypeConverter(final ConfiguredProjectsActions configuredProjectsActions, final NotificationTypesActions notificationTypesActions, final ContentConverter contentConverter) {
        this.configuredProjectsActions = configuredProjectsActions;
        this.notificationTypesActions = notificationTypesActions;
        this.contentConverter = contentConverter;
    }

    public Config populateCommonFieldsFromEntity(final CommonDistributionConfig channelConfig, final CommonDistributionConfigEntity commonEntity) {
        channelConfig.setId(contentConverter.getStringValue(commonEntity.getId()));
        channelConfig.setDistributionType(commonEntity.getDistributionType());
        channelConfig.setFilterByProject(contentConverter.getStringValue(commonEntity.getFilterByProject()));
        channelConfig.setProviderName(commonEntity.getProviderName());
        channelConfig.setFrequency(commonEntity.getFrequency().name());
        channelConfig.setName(commonEntity.getName());
        channelConfig.setConfiguredProjects(configuredProjectsActions.getConfiguredProjects(commonEntity));
        channelConfig.setNotificationTypes(notificationTypesActions.getNotificationTypes(commonEntity));
        return channelConfig;
    }
}
