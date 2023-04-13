package com.synopsys.integration.alert.channel.jira.server.model.enumeration;

public enum JiraServerAuthorizationMethod {
    BASIC("Basic"),
    PERSONAL_ACCESS_TOKEN("Personal Access Token");

    private final String displayName;

    JiraServerAuthorizationMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
