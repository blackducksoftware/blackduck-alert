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
package com.synopsys.integration.alert.provider.blackduck.processor.detail;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.factory.BlackDuckPropertiesFactory;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.manual.component.BomEditNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.HttpUrl;

@Component
public class BomEditNotificationDetailExtractor extends NotificationDetailExtractor<BomEditNotificationContent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final BlackDuckPropertiesFactory blackDuckPropertiesFactory;

    @Autowired
    public BomEditNotificationDetailExtractor(Gson gson, BlackDuckPropertiesFactory blackDuckPropertiesFactory) {
        super(NotificationType.BOM_EDIT, BomEditNotificationContent.class, gson);
        this.blackDuckPropertiesFactory = blackDuckPropertiesFactory;
    }

    @Override
    protected List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel, BomEditNotificationContent notificationContent) {
        return retrieveProjectName(alertNotificationModel.getProviderConfigId(), notificationContent.getProjectVersion())
                   .map(projectName -> DetailedNotificationContent.project(alertNotificationModel, notificationContent, projectName))
                   .map(List::of)
                   .orElse(List.of());
    }

    private Optional<String> retrieveProjectName(Long blackDuckConfigId, String projectVersionUrl) {
        Optional<BlackDuckProperties> optionalProperties = blackDuckPropertiesFactory.createPropertiesIfConfigExists(blackDuckConfigId);
        if (optionalProperties.isPresent()) {
            try {
                BlackDuckApiClient blackDuckApiClient = createBlackDuckApiClient(optionalProperties.get());
                ProjectVersionView projectVersion = blackDuckApiClient.getResponse(new HttpUrl(projectVersionUrl), ProjectVersionView.class);
                return blackDuckApiClient.getResponse(projectVersion, ProjectVersionView.PROJECT_LINK_RESPONSE).map(ProjectView::getName);
            } catch (IntegrationException e) {
                logger.error("Failed to connect to BlackDuck. Config ID: {}", blackDuckConfigId, e);
            }
        } else {
            logger.warn("No BlackDuck config with ID {} existed", blackDuckConfigId);
        }
        return Optional.empty();
    }

    private BlackDuckApiClient createBlackDuckApiClient(BlackDuckProperties blackDuckProperties) throws AlertException {
        Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);
        BlackDuckHttpClient blackDuckHttpClient = blackDuckProperties.createBlackDuckHttpClient(intLogger);
        BlackDuckServicesFactory blackDuckServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, intLogger);
        return blackDuckServicesFactory.getBlackDuckApiClient();
    }

}
