/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.model;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class ProviderUserModel extends AlertSerializableModel {
    private final String emailAddress;
    private final Boolean optOut;

    public ProviderUserModel(String emailAddress, Boolean optOut) {
        this.emailAddress = emailAddress;
        this.optOut = optOut;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public Boolean getOptOut() {
        return optOut;
    }

}
