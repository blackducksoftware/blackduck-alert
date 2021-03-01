/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.collector.builder.model;

import java.util.Optional;

public class ComponentData {
    private String componentName;
    private String componentVersionName;
    private String projectVersionUrl;
    private String projectComponentLink;

    public ComponentData(String componentName, String componentVersionName, String projectVersionUrl, String projectComponentLink) {
        this.componentName = componentName;
        this.componentVersionName = componentVersionName;
        this.projectVersionUrl = projectVersionUrl;
        this.projectComponentLink = projectComponentLink;
    }

    public String getComponentName() {
        return componentName;
    }

    public Optional<String> getComponentVersionName() {
        return Optional.ofNullable(componentVersionName);
    }

    public String getProjectVersionUrl() {
        return projectVersionUrl;
    }

    public String getProjectComponentLink() {
        return projectComponentLink;
    }
}
