/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.azure.boards.common.oauth;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.json.JsonString;
import com.google.api.client.util.Key;

public class AzureTokenResponse extends TokenResponse {

    // Azure OAuth returns the expires_in as a string not a numeric value.
    // need to override and create a custom TokenResponse to use the JsonString annotation.
    @Key("expires_in")
    @JsonString
    private Long expiresInSeconds;

    public AzureTokenResponse() {
        super();
    }

    @Override
    public AzureTokenResponse clone() {
        return (AzureTokenResponse) super.clone();
    }
}
