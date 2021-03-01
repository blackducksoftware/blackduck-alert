/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.enumeration;

public enum ComponentItemPriority {
    HIGHEST,
    HIGH,
    MEDIUM,
    LOW,
    LOWEST,
    NONE;

    public static final ComponentItemPriority findPriority(String priority) {
        String upperCasePriority = priority.toUpperCase();
        if ("CRITICAL".equals(upperCasePriority) || "BLOCKER".equals(upperCasePriority)) {
            return HIGHEST;
        }

        try {
            return ComponentItemPriority.valueOf(upperCasePriority);
        } catch (IllegalArgumentException ex) {
            // couldn't find the enum value default to STANDARD
            return NONE;
        }
    }
}
