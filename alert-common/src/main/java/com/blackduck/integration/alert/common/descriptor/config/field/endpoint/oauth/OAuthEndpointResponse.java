package com.blackduck.integration.alert.common.descriptor.config.field.endpoint.oauth;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class OAuthEndpointResponse extends AlertSerializableModel {
    private final boolean authenticated;
    private final String authorizationUrl;
    private final String message;

    public OAuthEndpointResponse(boolean authenticated, String authorizationUrl, String message) {
        this.authenticated = authenticated;
        this.authorizationUrl = authorizationUrl;
        this.message = message;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    public String getMessage() {
        return message;
    }

}
