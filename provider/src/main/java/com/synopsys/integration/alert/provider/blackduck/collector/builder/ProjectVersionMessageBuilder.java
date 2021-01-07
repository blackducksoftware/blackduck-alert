/**
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
package com.synopsys.integration.alert.provider.blackduck.collector.builder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.CommonMessageData;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.provider.blackduck.collector.util.OperationUtil;
import com.synopsys.integration.blackduck.api.manual.component.ProjectVersionNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.ProjectVersionNotificationView;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;

@Component
public class ProjectVersionMessageBuilder extends BlackDuckMessageBuilder<ProjectVersionNotificationView> {
    private final Logger logger = LoggerFactory.getLogger(ProjectVersionMessageBuilder.class);
    private final OperationUtil operationUtil;

    @Autowired
    public ProjectVersionMessageBuilder(OperationUtil operationUtil) {
        super(NotificationType.PROJECT_VERSION);
        this.operationUtil = operationUtil;
    }

    @Override
    public List<ProviderMessageContent> buildMessageContents(CommonMessageData commonMessageData, ProjectVersionNotificationView notificationView, BlackDuckServicesFactory blackDuckServicesFactory) {
        ProjectVersionNotificationContent notificationContent = notificationView.getContent();
        ItemOperation projectLevelAction = operationUtil.getItemOperation(notificationContent.getOperationType());

        try {
            ProviderMessageContent.Builder messageContentBuilder = new ProviderMessageContent.Builder();
            messageContentBuilder
                .applyCommonData(commonMessageData)
                .applyProject(MessageBuilderConstants.LABEL_PROJECT_NAME, notificationContent.getProjectName(), notificationContent.getProject())
                .applyProjectVersion(MessageBuilderConstants.LABEL_PROJECT_VERSION_NAME, notificationContent.getProjectVersionName(), notificationContent.getProjectVersion())
                .applyAction(projectLevelAction);
            return List.of(messageContentBuilder.build());
        } catch (AlertException e) {
            logger.error("Unable to build Project Version notification messages", e);
            return List.of();
        }
    }

}
