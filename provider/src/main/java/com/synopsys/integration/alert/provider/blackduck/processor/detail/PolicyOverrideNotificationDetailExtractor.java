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
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractor;
import com.synopsys.integration.alert.provider.blackduck.processor.model.PolicyOverrideUniquePolicyNotificationContent;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.api.manual.component.PolicyOverrideNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.PolicyOverrideNotificationView;

@Component
public class PolicyOverrideNotificationDetailExtractor extends NotificationDetailExtractor<PolicyOverrideNotificationContent, PolicyOverrideNotificationView> {
    @Autowired
    public PolicyOverrideNotificationDetailExtractor(Gson gson) {
        super(NotificationType.POLICY_OVERRIDE, PolicyOverrideNotificationView.class, gson);
    }

    @Override
    protected List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel, PolicyOverrideNotificationContent notificationContent) {
        return notificationContent.getPolicyInfos()
                   .stream()
                   .map(policyInfo -> createFlattenedContent(notificationContent, policyInfo))
                   .map(content -> DetailedNotificationContent.policy(alertNotificationModel, content, notificationContent.getProjectName(), content.getPolicyInfo().getPolicyName()))
                   .collect(Collectors.toList());
    }

    private PolicyOverrideUniquePolicyNotificationContent createFlattenedContent(PolicyOverrideNotificationContent notificationContent, PolicyInfo policyInfo) {
        return new PolicyOverrideUniquePolicyNotificationContent(
            notificationContent.getProjectName(),
            notificationContent.getProjectVersionName(),
            notificationContent.getProjectVersion(),
            notificationContent.getComponentName(),
            notificationContent.getComponentVersionName(),
            notificationContent.getFirstName(),
            notificationContent.getLastName(),
            policyInfo,
            policyInfo.getPolicy(),
            notificationContent.getBomComponentVersionPolicyStatus(),
            notificationContent.getBomComponent()
        );
    }

}
