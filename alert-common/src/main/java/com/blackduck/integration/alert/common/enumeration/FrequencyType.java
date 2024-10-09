package com.blackduck.integration.alert.common.enumeration;

public enum FrequencyType {
    DAILY("Daily"),
    REAL_TIME("Real Time");

    private final String displayName;

    FrequencyType(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
