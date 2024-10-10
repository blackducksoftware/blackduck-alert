/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.authentication.web;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.blackduck.integration.alert.common.rest.model.Config;

public class LoginConfig extends Config {
    private String alertUsername;

    // If this variable name changes be sure to change the value in the toString
    private String alertPassword;

    public LoginConfig() {
    }

    public LoginConfig(String alertUsername, String alertPassword) {
        super("1L");
        this.alertUsername = alertUsername;
        this.alertPassword = alertPassword;
    }

    public String getAlertUsername() {
        return alertUsername;
    }

    public String getAlertPassword() {
        return alertPassword;
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.JSON_STYLE)
                   .setExcludeFieldNames("alertPassword")
                   .toString();
    }
}
