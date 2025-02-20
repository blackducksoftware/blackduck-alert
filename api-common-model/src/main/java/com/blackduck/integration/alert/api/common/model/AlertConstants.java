/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.common.model;

public class AlertConstants {
    public static final String SYSTEM_PROPERTY_KEY_APP_HOME = "APP_HOME";
    public static final String ALERT_APPLICATION_NAME = "Alert";
    public static final String MASKED_VALUE = "*****";

    private AlertConstants() throws InstantiationException {
        throw new InstantiationException("Cannot instantiate instance of utility class '" + getClass().getName() + "'");
    }

}
