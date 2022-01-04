/*
 * api-common-model
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.common.model;

public class AlertConstants {
    public static final String SYSTEM_PROPERTY_KEY_APP_HOME = "APP_HOME";
    public static final String ALERT_APPLICATION_NAME = "Alert";

    private AlertConstants() throws InstantiationException {
        throw new InstantiationException("Cannot instantiate instance of utility class '" + getClass().getName() + "'");
    }

}
