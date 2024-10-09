package com.blackduck.integration.alert.channel.email.web;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class EmailAddressSelectOption extends AlertSerializableModel {
    private final String emailAddress;
    private final Boolean optOut;

    public EmailAddressSelectOption(String emailAddress, Boolean optOut) {
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
