/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.provider.state;

public abstract class ProviderProperties {
    public static final Long UNKNOWN_CONFIG_ID = -1L;
    public static final String UNKNOWN_CONFIG_NAME = "UNKNOWN CONFIGURATION";

    private final Long configId;
    private final boolean configEnabled;
    private final String configName;

    protected ProviderProperties(Long configId, boolean configEnabled, String configName) {
        this.configId = configId;
        this.configEnabled = configEnabled;
        this.configName = configName;
    }

    public Long getConfigId() {
        return configId;
    }

    public boolean isConfigEnabled() {
        return configEnabled;
    }

    public String getConfigName() {
        return configName;
    }

}
