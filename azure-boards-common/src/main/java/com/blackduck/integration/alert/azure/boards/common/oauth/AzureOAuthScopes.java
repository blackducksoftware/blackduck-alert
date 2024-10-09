package com.blackduck.integration.alert.azure.boards.common.oauth;

public enum AzureOAuthScopes {
    PROJECTS_READ("vso.project"),
    PROJECTS_WRITE("vso.project_write"),
    WORK_FULL("vso.work_full");

    private final String scope;

    AzureOAuthScopes(String scope) {
        this.scope = scope;
    }

    public String getScope() {
        return scope;
    }
}
