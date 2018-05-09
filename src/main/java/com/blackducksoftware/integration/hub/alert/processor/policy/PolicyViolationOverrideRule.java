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

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.notification.content.NotificationContentDetail;
import com.blackducksoftware.integration.hub.notification.content.PolicyOverrideNotificationContent;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;

public class PolicyViolationOverrideRule extends AbstractPolicyViolationRule {

    public PolicyViolationOverrideRule() {
        super(NotificationType.POLICY_OVERRIDE);
    }

    @Override
    public void apply(final Map<String, NotificationModel> modelMap, final CommonNotificationState commonNotificationState, final HubBucket bucket) {
        addOrRemoveIfExists(modelMap, commonNotificationState);
    }

    @Override
    public NotificationEntity createNotificationEntity(final CommonNotificationState commonNotificationState, final NotificationContentDetail notificationContentDetail) {
        final Date createdAt = commonNotificationState.getCreatedAt();
        final NotificationCategoryEnum notificationType = NotificationCategoryEnum.POLICY_VIOLATION_OVERRIDE;
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
        final PolicyOverrideNotificationContent content = (PolicyOverrideNotificationContent) commonNotificationState.getContent();
        final String policyRuleUser = StringUtils.join(" ", content.firstName, content.lastName);
        return new NotificationEntity(contentKey, createdAt, notificationType, projectName, projectUrl, projectVersion, projectVersionUrl, componentName, componentVersion, policyRuleName, policyRuleUser);
    }
}
