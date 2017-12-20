package com.blackducksoftware.integration.hub.alert.enumeration;

import org.apache.commons.lang3.StringUtils;

public enum StatusEnum {
    SUCCESS("Success"),
    FAILURE("Failure");

    private final String displayName;

    private StatusEnum(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static StatusEnum getStatusEnumFromString(final String enumName) {
        if (StringUtils.isNotBlank(enumName)) {
            for (final StatusEnum statusEnum : values()) {
                if (statusEnum.toString().equalsIgnoreCase(enumName)) {
                    return statusEnum;
                }
            }
        }
        throw new IllegalArgumentException(enumName + " is not a valid StatusEnum");
    }
}
