package com.blackduck.integration.alert.api.processor.extract.model.project;

public enum ComponentConcernType {
    POLICY("Policy"),
    UNKNOWN_VERSION("Estimated Security Risk"),
    VULNERABILITY("Vulnerability");

    private final String displayName;

    ComponentConcernType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
