package com.synopsys.integration.alert.provider.blackduck.collector.builder.model;

public class ComponentData {
    private String componentName;
    private String componentVersionName;
    private String projectVersionUrl;

    public ComponentData(String componentName, String componentVersionName, String projectVersionUrl) {
        this.componentName = componentName;
        this.componentVersionName = componentVersionName;
        this.projectVersionUrl = projectVersionUrl;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getComponentVersionName() {
        return componentVersionName;
    }

    public String getProjectVersionUrl() {
        return projectVersionUrl;
    }
}
