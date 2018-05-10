/**
 * hub-alert
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
package com.blackducksoftware.integration.hub.alert.processor.policy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.hub.alert.processor.NotificationProcessingModel;
import com.blackducksoftware.integration.hub.alert.processor.NotificationTypeProcessor;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.notification.content.NotificationContentDetail;
import com.blackducksoftware.integration.hub.notification.content.PolicyOverrideNotificationContent;

@Component
public class PolicyNotificationTypeProcessor extends NotificationTypeProcessor<NotificationProcessingModel> {

    @Autowired
    public PolicyNotificationTypeProcessor(final GlobalProperties globalProperties) {
        super(globalProperties, Arrays.asList(new PolicyViolationRule(globalProperties), new PolicyViolationClearedRule(globalProperties), new PolicyViolationOverrideRule(globalProperties)));
    }

    @Override
    protected List<NotificationModel> createModelList() {
        final List<NotificationModel> modelList = new ArrayList<>(getModelMap().size());

        getModelMap().entrySet().forEach(entry -> {
            modelList.add(createNotificationModel(entry.getValue()));
        });

        return modelList;
    }

    private NotificationModel createNotificationModel(final NotificationProcessingModel processingModel) {
        return new NotificationModel(createNotificationEntity(processingModel), Collections.emptyList());
    }

    private NotificationEntity createNotificationEntity(final NotificationProcessingModel processingModel) {
        final NotificationContentDetail notificationContentDetail = processingModel.getContentDetail();
        final CommonNotificationState commonNotificationState = processingModel.getCommonNotificationState();
        final Date createdAt = commonNotificationState.getCreatedAt();
        final NotificationCategoryEnum notificationType = processingModel.getNotificationType();
        final String contentKey = notificationContentDetail.getContentDetailKey();
        final String projectName = notificationContentDetail.getProjectName();
        final String projectUrl = null;
        final String projectVersion = notificationContentDetail.getProjectVersionName();
        final String projectVersionUrl = notificationContentDetail.getProjectVersion().get().uri;
        String componentName = null;
        if (notificationContentDetail.getComponentName().isPresent()) {
            componentName = notificationContentDetail.getComponentName().get();
        }
        String componentVersion = null;
        if (notificationContentDetail.getComponentVersionName().isPresent()) {
            componentVersion = notificationContentDetail.getComponentVersionName().get();
        }
        final String policyRuleName = notificationContentDetail.getPolicyName().get();
        String policyRuleUser = null;
        if (NotificationCategoryEnum.POLICY_VIOLATION_OVERRIDE.equals(notificationType)) {
            final PolicyOverrideNotificationContent content = (PolicyOverrideNotificationContent) commonNotificationState.getContent();
            policyRuleUser = StringUtils.join(" ", content.firstName, content.lastName);
        }
        return new NotificationEntity(contentKey, createdAt, notificationType, projectName, projectUrl, projectVersion, projectVersionUrl, componentName, componentVersion, policyRuleName, policyRuleUser);
    }

}
