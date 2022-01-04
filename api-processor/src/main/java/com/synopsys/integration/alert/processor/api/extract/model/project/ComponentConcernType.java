/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.extract.model.project;

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
