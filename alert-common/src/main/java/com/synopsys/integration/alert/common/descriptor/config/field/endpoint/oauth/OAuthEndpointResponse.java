/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field.endpoint.oauth;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

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
