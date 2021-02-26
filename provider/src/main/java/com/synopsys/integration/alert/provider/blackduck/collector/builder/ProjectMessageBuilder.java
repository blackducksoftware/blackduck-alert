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
import com.synopsys.integration.blackduck.api.manual.component.ProjectNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.ProjectNotificationView;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;

@Component
public class ProjectMessageBuilder extends BlackDuckMessageBuilder<ProjectNotificationView> {
    private final Logger logger = LoggerFactory.getLogger(ProjectMessageBuilder.class);
    private final OperationUtil operationUtil;

    @Autowired
    public ProjectMessageBuilder(OperationUtil operationUtil) {
        super(NotificationType.PROJECT);
        this.operationUtil = operationUtil;
    }

    @Override
    public List<ProviderMessageContent> buildMessageContents(CommonMessageData commonMessageData, ProjectNotificationView notificationView, BlackDuckServicesFactory blackDuckServicesFactory) {
        ProjectNotificationContent notificationContent = notificationView.getContent();
        ItemOperation projectLevelAction = operationUtil.getItemOperation(notificationContent.getOperationType());

        try {
            ProviderMessageContent.Builder messageContentBuilder = new ProviderMessageContent.Builder();
            messageContentBuilder
                .applyCommonData(commonMessageData)
                .applyTopic(MessageBuilderConstants.LABEL_PROJECT_NAME, notificationContent.getProjectName(), notificationContent.getProject())
                .applyAction(projectLevelAction);

            return List.of(messageContentBuilder.build());
        } catch (AlertException e) {
            logger.error("Unable to build Project notification messages", e);
            return List.of();
        }
    }

}
