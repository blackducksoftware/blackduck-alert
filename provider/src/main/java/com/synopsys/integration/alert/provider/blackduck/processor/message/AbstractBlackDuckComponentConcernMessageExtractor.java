/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.provider.blackduck.processor.message;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.processor.api.extract.ProviderMessageExtractor;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.filter.NotificationContentWrapper;
import com.synopsys.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.synopsys.integration.alert.provider.blackduck.processor.model.AbstractProjectVersionNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;

public abstract class AbstractBlackDuckComponentConcernMessageExtractor<T extends AbstractProjectVersionNotificationContent> extends ProviderMessageExtractor<T> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final BlackDuckProviderKey blackDuckProviderKey;
    private final NotificationExtractorBlackDuckServicesFactoryCache servicesFactoryCache;

    public AbstractBlackDuckComponentConcernMessageExtractor(
        NotificationType notificationType,
        Class<T> notificationContentClass,
        BlackDuckProviderKey blackDuckProviderKey,
        NotificationExtractorBlackDuckServicesFactoryCache servicesFactoryCache
    ) {
        super(notificationType, notificationContentClass);
        this.blackDuckProviderKey = blackDuckProviderKey;
        this.servicesFactoryCache = servicesFactoryCache;
    }

    @Override
    protected final ProviderMessageHolder extract(NotificationContentWrapper notificationContentWrapper, T notificationContent) {
        AlertNotificationModel notificationModel = notificationContentWrapper.getAlertNotificationModel();

        List<BomComponentDetails> bomComponentDetails;
        Long providerConfigId = notificationModel.getProviderConfigId();
        try {
            BlackDuckServicesFactory blackDuckServicesFactory = servicesFactoryCache.retrieveBlackDuckServicesFactory(providerConfigId);
            bomComponentDetails = createBomComponentDetails(notificationContent, blackDuckServicesFactory);
        } catch (AlertConfigurationException e) {
            logger.warn("Invalid BlackDuck configuration for notification. ID: {}. Name: {}", providerConfigId, notificationModel.getProviderConfigName(), e);
            return ProviderMessageHolder.empty();
        } catch (IntegrationException e) {
            logger.warn("Failed to retrieve BOM Component(s) from BlackDuck", e);
            return ProviderMessageHolder.empty();
        }

        LinkableItem provider = new LinkableItem(blackDuckProviderKey.getDisplayName(), notificationModel.getProviderConfigName());
        LinkableItem project = new LinkableItem(BlackDuckMessageLabels.LABEL_PROJECT, notificationContent.getProjectName());
        LinkableItem projectVersion = new LinkableItem(BlackDuckMessageLabels.LABEL_PROJECT_VERSION, notificationContent.getProjectVersionName(), notificationContent.getProjectVersion());

        ProjectMessage projectMessage = ProjectMessage.componentConcern(provider, project, projectVersion, bomComponentDetails);
        return new ProviderMessageHolder(List.of(projectMessage), List.of());
    }

    protected abstract List<BomComponentDetails> createBomComponentDetails(T notificationContent, BlackDuckServicesFactory blackDuckServicesFactory) throws IntegrationException;

}
