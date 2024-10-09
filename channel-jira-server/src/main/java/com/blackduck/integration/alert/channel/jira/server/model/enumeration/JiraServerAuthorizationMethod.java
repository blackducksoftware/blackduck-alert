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
