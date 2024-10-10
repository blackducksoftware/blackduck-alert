/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.model.enumeration;

public enum JiraServerAuthorizationMethod {
    BASIC(0, "Basic"),
    PERSONAL_ACCESS_TOKEN(1, "Personal Access Token");

    private final Integer mode;
    private final String displayName;

    JiraServerAuthorizationMethod(Integer mode, String displayName) {
        this.mode = mode;
        this.displayName = displayName;
    }

    public Integer getMode() {
        return mode;
    }

    public String getDisplayName() {
        return displayName;
    }
}
