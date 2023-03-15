package com.synopsys.integration.alert.authentication.ldap.model;

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
