/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.web;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class EmailAddressSelectOption extends AlertSerializableModel {
    private final String userName;
    private final String emailAddress;
    private final Boolean optOut;

    public EmailAddressSelectOption(String userName, String emailAddress, Boolean optOut) {
        this.userName = userName;
        this.emailAddress = emailAddress;
        this.optOut = optOut;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public Boolean getOptOut() {
        return optOut;
    }

}
