/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.web.api.home;

import java.io.Serial;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class VerifyAuthenticationResponseModel extends AlertSerializableModel {
    @Serial
    private static final long serialVersionUID = -513519901474390784L;
    
    public final boolean authenticated;

    public VerifyAuthenticationResponseModel() {
        this.authenticated = false;
    }

    public VerifyAuthenticationResponseModel(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
