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
package com.synopsys.integration.alert.processor.api.filter.model;

import java.util.List;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.blackduck.api.manual.component.NotificationContentComponent;

public class FilterableNotificationWrapper extends ProcessableNotificationWrapper {
    private final String projectName;
    private final List<String> policyNames;
    private final List<String> vulnerabilitySeverities;

    public static FilterableNotificationWrapper vulnerability(
        AlertNotificationModel notificationModel,
        NotificationContentComponent notificationContent,
        String projectName,
        List<String> vulnerabilitySeverities
    ) {
        return new FilterableNotificationWrapper(notificationModel, notificationContent, projectName, List.of(), vulnerabilitySeverities);
    }

    public static FilterableNotificationWrapper policy(
        AlertNotificationModel notificationModel,
        NotificationContentComponent notificationContent,
        String projectName,
        List<String> policyNames
    ) {
        return new FilterableNotificationWrapper(notificationModel, notificationContent, projectName, policyNames, List.of());
    }

    public static FilterableNotificationWrapper project(AlertNotificationModel notificationModel, NotificationContentComponent notificationContent, String projectName) {
        return new FilterableNotificationWrapper(notificationModel, notificationContent, projectName, List.of(), List.of());
    }

    public static FilterableNotificationWrapper projectless(AlertNotificationModel notificationModel, NotificationContentComponent notificationContent) {
        return new FilterableNotificationWrapper(notificationModel, notificationContent, null, List.of(), List.of());
    }

    private FilterableNotificationWrapper(AlertNotificationModel alertNotificationModel, NotificationContentComponent notificationContent, String projectName, List<String> policyNames, List<String> vulnerabilitySeverities) {
        super(alertNotificationModel, notificationContent, notificationContent.getClass());
        this.projectName = projectName;
        this.policyNames = policyNames;
        this.vulnerabilitySeverities = vulnerabilitySeverities;
    }

    public String getProjectName() {
        return projectName;
    }

    public List<String> getPolicyNames() {
        return policyNames;
    }

    public List<String> getVulnerabilitySeverities() {
        return vulnerabilitySeverities;
    }

}
