/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.authentication.web;

import java.io.Serial;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class AuthenticationResponseModel extends AlertSerializableModel {

    @Serial
    private static final long serialVersionUID = -8585360211505351102L;

    private final int statusCode;
    private final String message;

    public AuthenticationResponseModel(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}
