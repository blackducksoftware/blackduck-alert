/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.provider.state;

import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;

public abstract class ProviderProperties {
    public static final Long UNKNOWN_CONFIG_ID = -1L;
    public static final String UNKNOWN_CONFIG_NAME = "UNKNOWN CONFIGURATION";
    private Long configId;
    private boolean configEnabled;
    private String configName;

    public ProviderProperties(Long configId, FieldUtility fieldUtility) {
        this.configId = configId;
        this.configEnabled = fieldUtility.getBooleanOrFalse(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED);
        this.configName = fieldUtility.getString(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME).orElse(UNKNOWN_CONFIG_NAME);
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
