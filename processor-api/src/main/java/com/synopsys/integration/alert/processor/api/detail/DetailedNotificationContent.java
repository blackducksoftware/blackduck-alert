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
package com.synopsys.integration.alert.processor.api.detail;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.alert.processor.api.filter.NotificationContentWrapper;
import com.synopsys.integration.blackduck.api.manual.component.NotificationContentComponent;

public class DetailedNotificationContent extends AlertSerializableModel {
    private final String projectName;
    private final String policyName;
    private final List<String> vulnerabilitySeverities;
    private final NotificationContentWrapper notificationContentWrapper;

    public static DetailedNotificationContent vulnerability(
        AlertNotificationModel notificationModel,
        NotificationContentComponent notificationContent,
        String projectName,
        List<String> vulnerabilitySeverities
    ) {
        return new DetailedNotificationContent(notificationModel, notificationContent, projectName, null, vulnerabilitySeverities);
    }

    public static DetailedNotificationContent policy(
        AlertNotificationModel notificationModel,
        NotificationContentComponent notificationContent,
        String projectName,
        String policyName
    ) {
        return new DetailedNotificationContent(notificationModel, notificationContent, projectName, policyName, List.of());
    }

    public static DetailedNotificationContent project(AlertNotificationModel notificationModel, NotificationContentComponent notificationContent, String projectName) {
        return new DetailedNotificationContent(notificationModel, notificationContent, projectName, null, List.of());
    }

    public static DetailedNotificationContent projectless(AlertNotificationModel notificationModel, NotificationContentComponent notificationContent) {
        return new DetailedNotificationContent(notificationModel, notificationContent, null, null, List.of());
    }

    private DetailedNotificationContent(
        AlertNotificationModel alertNotificationModel,
        NotificationContentComponent notificationContent,
        @Nullable String projectName,
        @Nullable String policyName,
        List<String> vulnerabilitySeverities
    ) {
        this.projectName = StringUtils.trimToNull(projectName);
        this.policyName = StringUtils.trimToNull(policyName);
        this.vulnerabilitySeverities = vulnerabilitySeverities;
        this.notificationContentWrapper = new NotificationContentWrapper(alertNotificationModel, notificationContent, notificationContent.getClass());
    }

    public Optional<String> getProjectName() {
        return Optional.ofNullable(projectName);
    }

    public Optional<String> getPolicyName() {
        return Optional.ofNullable(policyName);
    }

    public List<String> getVulnerabilitySeverities() {
        return vulnerabilitySeverities;
    }

    public NotificationContentWrapper getNotificationContentWrapper() {
        return notificationContentWrapper;
    }

}
