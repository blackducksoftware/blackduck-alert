/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.rest;

public final class AlertRestConstants {
    public static final String API = "api";
    public static final String CALLBACKS = "callbacks";
    public static final String OAUTH = "oauth";
    public static final String UPLOAD = "upload";
    public static final String BASE_PATH = "/" + API;
    public static final String CALLBACKS_PATH = BASE_PATH + "/" + CALLBACKS;
    public static final String OAUTH_CALLBACK_PATH = CALLBACKS_PATH + "/" + OAUTH;
    public static final String CERTIFICATE_PATH = AlertRestConstants.BASE_PATH + "/certificates";
    public static final String CLIENT_CERTIFICATE_PATH = AlertRestConstants.CERTIFICATE_PATH + "/mtls/client";
    public static final String CONFIGURATION_PATH = AlertRestConstants.BASE_PATH + "/configuration";
    public static final String AZURE_BOARDS_CONFIGURATION_PATH = AlertRestConstants.CONFIGURATION_PATH + "/azure-boards";
    public static final String AZURE_BOARDS_OAUTH_CALLBACK_PATH = AlertRestConstants.OAUTH_CALLBACK_PATH + "/azure";
    public static final String EMAIL_CONFIGURATION_PATH = AlertRestConstants.CONFIGURATION_PATH + "/email";
    public static final String JIRA_SERVER_CONFIGURATION_PATH = AlertRestConstants.CONFIGURATION_PATH + "/jira_server";
    public static final String SETTINGS_PATH = AlertRestConstants.BASE_PATH + "/settings";
    public static final String SETTINGS_ENCRYPTION_PATH = AlertRestConstants.SETTINGS_PATH + "/encryption";
    public static final String SETTINGS_PROXY_PATH = AlertRestConstants.SETTINGS_PATH + "/proxy";
    public static final String DIAGNOSTIC_PATH = AlertRestConstants.BASE_PATH + "/diagnostic";
    public static final String AUTHENTICATION_PATH = AlertRestConstants.BASE_PATH + "/authentication";
    public static final String SAML_PATH = AlertRestConstants.AUTHENTICATION_PATH + "/saml";
    public static final String LDAP_PATH = AlertRestConstants.AUTHENTICATION_PATH + "/ldap";

    public static final String DEFAULT_CONFIGURATION_NAME = "default-configuration";
    public static final String DEFAULT_CLIENT_CERTIFICATE_ALIAS = "default-alert-client-certificate";

    public static final String SWAGGER_PATH = "/v3/api-docs/production";

    private AlertRestConstants() {
        // This class should not be instantiated
    }

}
