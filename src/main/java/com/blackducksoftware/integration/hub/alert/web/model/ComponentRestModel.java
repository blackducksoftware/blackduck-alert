package com.blackducksoftware.integration.hub.alert.web.model;

public class ComponentRestModel {
    private String componentName;
    private String componentVersion;

    public ComponentRestModel() {
    }

    public ComponentRestModel(final String componentName, final String componentVersion) {
        this.componentName = componentName;
        this.componentVersion = componentVersion;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(final String componentName) {
        this.componentName = componentName;
    }

    public String getComponentVersion() {
        return componentVersion;
    }

    public void setComponentVersion(final String componentVersion) {
        this.componentVersion = componentVersion;
    }

}
