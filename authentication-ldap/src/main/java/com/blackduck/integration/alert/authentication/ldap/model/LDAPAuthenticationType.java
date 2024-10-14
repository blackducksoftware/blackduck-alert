/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.authentication.ldap.model;

import org.apache.commons.lang3.StringUtils;

public enum LDAPAuthenticationType {
    SIMPLE("simple"),
    DIGEST("digest"),
    NONE("none");

    private final String authenticationType;

    LDAPAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public static String fromString(String authenticationType) {
        if (StringUtils.isNotBlank(authenticationType)) {
            try {
                return LDAPAuthenticationType.valueOf(authenticationType.toUpperCase()).getAuthenticationType();
            } catch (IllegalArgumentException e) {
                // Ignored
            }
        }
        return LDAPAuthenticationType.SIMPLE.getAuthenticationType();
    }

    public String getAuthenticationType() {
        return authenticationType.toLowerCase();
    }
}
