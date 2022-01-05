/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

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
