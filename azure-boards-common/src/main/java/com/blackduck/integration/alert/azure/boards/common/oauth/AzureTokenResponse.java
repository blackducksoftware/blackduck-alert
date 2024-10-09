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
