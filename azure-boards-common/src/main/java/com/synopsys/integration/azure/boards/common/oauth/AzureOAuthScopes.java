/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.oauth;

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
