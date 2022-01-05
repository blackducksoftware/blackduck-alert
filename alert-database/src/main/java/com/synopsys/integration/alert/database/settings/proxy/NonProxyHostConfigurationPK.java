/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.settings.proxy;

import java.io.Serializable;
import java.util.UUID;

public class NonProxyHostConfigurationPK implements Serializable {
    private static final long serialVersionUID = 1066613909027338545L;
    private UUID configurationId;
    private String hostnamePattern;

    public NonProxyHostConfigurationPK() {
    }

    public NonProxyHostConfigurationPK(UUID configurationId, String hostnamePattern) {
        this.configurationId = configurationId;
        this.hostnamePattern = hostnamePattern;
    }

    public UUID getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(UUID configurationId) {
        this.configurationId = configurationId;
    }

    public String getHostnamePattern() {
        return hostnamePattern;
    }

    public void setHostnamePattern(String hostnamePattern) {
        this.hostnamePattern = hostnamePattern;
    }
}
