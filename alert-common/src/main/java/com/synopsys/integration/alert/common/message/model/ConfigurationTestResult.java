package com.synopsys.integration.alert.common.message.model;

public class ConfigurationTestResult {
    private final boolean isSuccess;
    private final String statusMessage;

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
