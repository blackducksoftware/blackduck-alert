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
