/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
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
