/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.processor.model;

import com.blackduck.integration.blackduck.api.manual.component.BomEditNotificationContent;

public class BomEditWithProjectNameNotificationContent extends AbstractProjectVersionNotificationContent {
    private final String bomComponent;
    private final String componentName;
    private final String componentVersionName;

    public BomEditWithProjectNameNotificationContent(BomEditNotificationContent sourceNotificationContent,
        String projectName,
        String projectVersionName
    ) {
        super(projectName, projectVersionName, sourceNotificationContent.getProjectVersion());
        this.bomComponent = sourceNotificationContent.getBomComponent();
        this.componentName = sourceNotificationContent.getComponentName();
        this.componentVersionName = sourceNotificationContent.getComponentVersionName();
    }

    public String getBomComponent() {
        return bomComponent;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getComponentVersionName() {
        return componentVersionName;
    }

}
