package com.synopsys.integration.alert.authentication.ldap.model;

import org.apache.commons.lang3.StringUtils;

public enum LDAPAuthenticationType {
    simple,
    digest,
    none;

    public static String fromString(String authenticationType) {
        if (StringUtils.isNotBlank(authenticationType)) {
            try {
                return LDAPAuthenticationType.valueOf(authenticationType.toLowerCase()).toString();
            } catch (IllegalArgumentException e) {
                // Ignored
            }
        }
        return LDAPAuthenticationType.simple.toString();
    }
}
