/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.azure.boards.common.oauth;

public class AzureOAuthConstants {
    public static final String DEFAULT_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";
    public static final String DEFAULT_CLIENT_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

    public static final String REQUEST_BODY_FIELD_ASSERTION = "assertion";
    public static final String REQUEST_BODY_FIELD_CLIENT_ASSERTION_TYPE = "client_assertion_type";
    public static final String REQUEST_BODY_FIELD_CLIENT_ASSERTION = "client_assertion";
    public static final String REQUEST_BODY_FIELD_REDIRECT_URI = "redirect_uri";

    private AzureOAuthConstants() {
        // hiding constructor only constants.
    }
}
