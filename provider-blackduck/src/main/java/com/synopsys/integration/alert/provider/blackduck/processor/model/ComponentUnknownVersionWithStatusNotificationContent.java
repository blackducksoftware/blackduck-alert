/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.model;

import com.synopsys.integration.blackduck.api.manual.enumeration.ComponentUnknownVersionStatus;

public class ComponentUnknownVersionWithStatusNotificationContent extends AbstractProjectVersionNotificationContent {
    private String componentName;
    private String bomComponent;
    private String component;
    private int criticalVulnerabilityCount;
    private String criticalVulnerabilityVersion;
    private String criticalVulnerabilityVersionName;
    private int highVulnerabilityCount;
    private String highVulnerabilityVersion;
    private String highVulnerabilityVersionName;
    private int mediumVulnerabilityCount;
    private String mediumVulnerabilityVersion;
    private String mediumVulnerabilityVersionName;
    private int lowVulnerabilityCount;
    private String lowVulnerabilityVersion;
    private String lowVulnerabilityVersionName;
    private ComponentUnknownVersionStatus status;

    public ComponentUnknownVersionWithStatusNotificationContent(String projectName, String projectVersionName, String projectVersionUrl,
        String componentName, String bomComponent, String component,
        int criticalVulnerabilityCount, String criticalVulnerabilityVersion, String criticalVulnerabilityVersionName,
        int highVulnerabilityCount, String highVulnerabilityVersion, String highVulnerabilityVersionName,
        int mediumVulnerabilityCount, String mediumVulnerabilityVersion, String mediumVulnerabilityVersionName,
        int lowVulnerabilityCount, String lowVulnerabilityVersion, String lowVulnerabilityVersionName,
        ComponentUnknownVersionStatus status) {
        super(projectName, projectVersionName, projectVersionUrl);
        this.componentName = componentName;
        this.bomComponent = bomComponent;
        this.component = component;
        this.criticalVulnerabilityCount = criticalVulnerabilityCount;
        this.criticalVulnerabilityVersion = criticalVulnerabilityVersion;
        this.criticalVulnerabilityVersionName = criticalVulnerabilityVersionName;
        this.highVulnerabilityCount = highVulnerabilityCount;
        this.highVulnerabilityVersion = highVulnerabilityVersion;
        this.highVulnerabilityVersionName = highVulnerabilityVersionName;
        this.mediumVulnerabilityCount = mediumVulnerabilityCount;
        this.mediumVulnerabilityVersion = mediumVulnerabilityVersion;
        this.mediumVulnerabilityVersionName = mediumVulnerabilityVersionName;
        this.lowVulnerabilityCount = lowVulnerabilityCount;
        this.lowVulnerabilityVersion = lowVulnerabilityVersion;
        this.lowVulnerabilityVersionName = lowVulnerabilityVersionName;
        this.status = status;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getBomComponent() {
        return bomComponent;
    }

    public String getComponent() {
        return component;
    }

    public int getCriticalVulnerabilityCount() {
        return criticalVulnerabilityCount;
    }

    public String getCriticalVulnerabilityVersion() {
        return criticalVulnerabilityVersion;
    }

    public String getCriticalVulnerabilityVersionName() {
        return criticalVulnerabilityVersionName;
    }

    public int getHighVulnerabilityCount() {
        return highVulnerabilityCount;
    }

    public String getHighVulnerabilityVersion() {
        return highVulnerabilityVersion;
    }

    public String getHighVulnerabilityVersionName() {
        return highVulnerabilityVersionName;
    }

    public int getMediumVulnerabilityCount() {
        return mediumVulnerabilityCount;
    }

    public String getMediumVulnerabilityVersion() {
        return mediumVulnerabilityVersion;
    }

    public String getMediumVulnerabilityVersionName() {
        return mediumVulnerabilityVersionName;
    }

    public int getLowVulnerabilityCount() {
        return lowVulnerabilityCount;
    }

    public String getLowVulnerabilityVersion() {
        return lowVulnerabilityVersion;
    }

    public String getLowVulnerabilityVersionName() {
        return lowVulnerabilityVersionName;
    }

    public ComponentUnknownVersionStatus getStatus() {
        return status;
    }
}
