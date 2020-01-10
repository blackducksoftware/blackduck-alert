/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.provider.blackduck.collector.builder;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.provider.blackduck.collector.util.OperationUtil;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.component.ProjectVersionNotificationContent;
import com.synopsys.integration.blackduck.api.manual.view.ProjectVersionNotificationView;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;

@Component
public class ProjectVersionMessageBuilder implements BlackDuckMessageBuilder<ProjectVersionNotificationView> {
    private final Logger logger = LoggerFactory.getLogger(ProjectVersionMessageBuilder.class);
    private OperationUtil operationUtil;

    @Autowired
    public ProjectVersionMessageBuilder(OperationUtil operationUtil) {
        this.operationUtil = operationUtil;
    }

    @Override
    public String getNotificationType() {
        return NotificationType.PROJECT_VERSION.name();
    }

    @Override
    public List<ProviderMessageContent> buildMessageContents(Long notificationId, Date providerCreationDate, ConfigurationJobModel job, ProjectVersionNotificationView notificationView, BlackDuckBucket blackDuckBucket,
        BlackDuckServicesFactory blackDuckServicesFactory) {
        ProjectVersionNotificationContent notificationContent = notificationView.getContent();
        ItemOperation projectLevelAction = operationUtil.getItemOperation(notificationContent.getOperationType());

        String projectUrl = null;
        String projectVersionUrl = null;
        if (!projectLevelAction.equals(ItemOperation.DELETE)) {
            projectUrl = notificationContent.getProject();
            projectVersionUrl = notificationContent.getProjectVersion();
        }

        try {
            ProviderMessageContent.Builder projectVersionMessageBuilder = new ProviderMessageContent.Builder()
                                                                              .applyProvider(getProviderName(), blackDuckServicesFactory.getBlackDuckHttpClient().getBaseUrl())
                                                                              .applyTopic(MessageBuilderConstants.LABEL_PROJECT_NAME, notificationContent.getProjectName(), projectUrl)
                                                                              .applySubTopic(MessageBuilderConstants.LABEL_PROJECT_VERSION_NAME, notificationContent.getProjectVersionName(), projectVersionUrl)
                                                                              .applyAction(projectLevelAction)
                                                                              .applyNotificationId(notificationId)
                                                                              .applyProviderCreationTime(providerCreationDate);
            return List.of(projectVersionMessageBuilder.build());
        } catch (AlertException e) {
            logger.error("Unable to build Project Version notification messages", e);
            return List.of();
        }
    }

}
