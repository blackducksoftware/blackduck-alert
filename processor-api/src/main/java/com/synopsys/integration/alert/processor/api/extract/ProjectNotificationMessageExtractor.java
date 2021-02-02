/*
 * processor-api
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
package com.synopsys.integration.alert.processor.api.extract;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectOperation;
import com.synopsys.integration.alert.processor.api.filter.NotificationContentWrapper;
import com.synopsys.integration.blackduck.api.manual.component.ProjectNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.enumeration.OperationType;

@Component
public class ProjectNotificationMessageExtractor extends ProviderMessageExtractor<ProjectNotificationContent> {
    public static final String LABEL_PROJECT = "Project";

    private final BlackDuckProviderKey blackDuckProviderKey;

    @Autowired
    public ProjectNotificationMessageExtractor(BlackDuckProviderKey blackDuckProviderKey) {
        super(NotificationType.PROJECT, ProjectNotificationContent.class);
        this.blackDuckProviderKey = blackDuckProviderKey;
    }

    @Override
    protected ProviderMessageHolder extract(NotificationContentWrapper notificationContentWrapper, ProjectNotificationContent notificationContent) {
        AlertNotificationModel alertNotificationModel = notificationContentWrapper.getAlertNotificationModel();

        LinkableItem provider = new LinkableItem(blackDuckProviderKey.getDisplayName(), alertNotificationModel.getProviderConfigName());
        LinkableItem project = new LinkableItem(LABEL_PROJECT, notificationContent.getProjectName(), notificationContent.getProject());

        OperationType notificationOperation = notificationContent.getOperationType();
        ProjectOperation operation = OperationType.DELETE.equals(notificationOperation) ? ProjectOperation.DELETE : ProjectOperation.CREATE;

        ProjectMessage projectMessage = ProjectMessage.projectStatusInfo(provider, project, operation);
        return new ProviderMessageHolder(List.of(projectMessage), List.of());
    }

}
