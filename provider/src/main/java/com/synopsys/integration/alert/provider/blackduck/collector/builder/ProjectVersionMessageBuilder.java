/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
                .applyTopic(MessageBuilderConstants.LABEL_PROJECT_NAME, notificationContent.getProjectName(), notificationContent.getProject())
                .applySubTopic(MessageBuilderConstants.LABEL_PROJECT_VERSION_NAME, notificationContent.getProjectVersionName(), notificationContent.getProjectVersion())
                .applyAction(projectLevelAction);
            return List.of(messageContentBuilder.build());
        } catch (AlertException e) {
            logger.error("Unable to build Project Version notification messages", e);
            return List.of();
        }
    }

}
