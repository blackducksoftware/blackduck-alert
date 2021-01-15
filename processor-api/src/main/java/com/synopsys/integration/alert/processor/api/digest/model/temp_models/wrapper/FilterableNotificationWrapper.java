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
package com.synopsys.integration.alert.processor.api.digest.model.temp_models.wrapper;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.blackduck.api.manual.component.NotificationContentComponent;
import com.synopsys.integration.blackduck.api.manual.component.VulnerabilityNotificationContent;

public class FilterableNotificationWrapper<T extends NotificationContentComponent> extends ProcessableNotificationWrapper<T> {
    private @Nullable
    String projectName = null;
    private @Nullable
    String policyName = null;
    private List<String> vulnerabilitySeverities = List.of();

    public static FilterableNotificationWrapper<VulnerabilityNotificationContent> vulnerability(
        AlertNotificationModel notificationModel,
        VulnerabilityNotificationContent notificationContent,
        String projectName,
        List<String> vulnerabilitySeverities
    ) {
        FilterableNotificationWrapper<VulnerabilityNotificationContent> wrapper = new FilterableNotificationWrapper<>(notificationModel, notificationContent);
        wrapper.projectName = projectName;
        wrapper.vulnerabilitySeverities = vulnerabilitySeverities;
        return wrapper;
    }

    public static FilterableNotificationWrapper<NotificationContentComponent> policy(
        AlertNotificationModel notificationModel,
        NotificationContentComponent notificationContent,
        String projectName,
        String policyName
    ) {
        FilterableNotificationWrapper<NotificationContentComponent> wrapper = new FilterableNotificationWrapper<>(notificationModel, notificationContent);
        wrapper.projectName = projectName;
        wrapper.policyName = policyName;
        return wrapper;
    }

    public static FilterableNotificationWrapper<NotificationContentComponent> project(
        AlertNotificationModel notificationModel,
        NotificationContentComponent notificationContent,
        String projectName
    ) {
        FilterableNotificationWrapper<NotificationContentComponent> wrapper = new FilterableNotificationWrapper<>(notificationModel, notificationContent);
        wrapper.projectName = projectName;
        return wrapper;
    }

    public static FilterableNotificationWrapper<NotificationContentComponent> projectless(
        AlertNotificationModel notificationModel,
        NotificationContentComponent notificationContent
    ) {
        return new FilterableNotificationWrapper<>(notificationModel, notificationContent);
    }

    private FilterableNotificationWrapper(AlertNotificationModel alertNotificationModel, T notificationContent) {
        super(alertNotificationModel, notificationContent);
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

}
