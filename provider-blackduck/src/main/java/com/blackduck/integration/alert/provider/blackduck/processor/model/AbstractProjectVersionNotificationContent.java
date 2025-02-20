/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.processor.model;

import com.blackduck.integration.alert.provider.blackduck.processor.message.util.BlackDuckMessageLinkUtils;
import com.blackduck.integration.blackduck.api.manual.component.NotificationContentComponent;

public abstract class AbstractProjectVersionNotificationContent extends NotificationContentComponent {
    private final String projectName;
    private final String projectVersionName;
    private final String projectVersionUrl;

    public AbstractProjectVersionNotificationContent(String projectName, String projectVersionName, String projectVersionUrl) {
        this.projectName = projectName;
        this.projectVersionName = projectVersionName;
        this.projectVersionUrl = projectVersionUrl;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersionName() {
        return projectVersionName;
    }

    public String getProjectVersionUrl() {
        return projectVersionUrl;
    }

    public String getProjectVersionComponentsTabUrl() { return getProjectVersionUrl() + BlackDuckMessageLinkUtils.URI_PIECE_COMPONENTS; }
}
