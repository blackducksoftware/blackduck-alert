/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.enumeration;

public enum AuditEntryStatus {
    PENDING("Pending"),
    SUCCESS("Success"),
    FAILURE("Failure");

    private final String displayName;

    private AuditEntryStatus(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
