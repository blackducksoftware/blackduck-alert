/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest;

public final class AlertRestConstants {
    public static final String API = "api";
    public static final String CALLBACKS = "callbacks";
    public static final String OAUTH = "oauth";
    public static final String BASE_PATH = "/" + API;
    public static final String CALLBACKS_PATH = BASE_PATH + "/" + CALLBACKS;
    public static final String OAUTH_CALLBACK_PATH = CALLBACKS_PATH + "/" + OAUTH;
    public static final String CONFIGURATION_PATH = AlertRestConstants.BASE_PATH + "/configuration";
    public static final String EMAIL_CONFIGURATION_PATH = AlertRestConstants.CONFIGURATION_PATH + "/email";
    public static final String SETTINGS_PATH = AlertRestConstants.BASE_PATH + "/settings";
    public static final String SETTINGS_ENCRYPTION_PATH = AlertRestConstants.SETTINGS_PATH + "/encryption";
    public static final String SETTINGS_PROXY_PATH = AlertRestConstants.SETTINGS_PATH + "/proxy";

    public static final String MASKED_VALUE = "*****";
    public static final String DEFAULT_CONFIGURATION_NAME = "default-configuration";

    private AlertRestConstants() {
        // This class should not be instantiated
    }

}
