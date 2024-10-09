/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.message.model;

public class ConfigurationTestResult {
    private static final String STATUS_MESSAGE_SUCCESS = "Success";

    private final boolean isSuccess;
    private final String statusMessage;

    public static ConfigurationTestResult success() {
        return new ConfigurationTestResult(true, STATUS_MESSAGE_SUCCESS);
    }

    public static ConfigurationTestResult success(String statusMessage) {
        return new ConfigurationTestResult(true, statusMessage);
    }

    public static ConfigurationTestResult failure(String statusMessage) {
        return new ConfigurationTestResult(false, statusMessage);
    }

    private ConfigurationTestResult(boolean isSuccess, String statusMessage) {
        this.isSuccess = isSuccess;
        this.statusMessage = statusMessage;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getStatusMessage() {
        return statusMessage;
    }
}
