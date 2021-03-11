/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.factory;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.provider.lifecycle.ProviderPropertiesFactory;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;

@Component
public class BlackDuckPropertiesFactory extends ProviderPropertiesFactory<BlackDuckProperties> {
    private final Gson gson;
    private final AlertProperties alertProperties;
    private final ProxyManager proxyManager;

    @Autowired
    public BlackDuckPropertiesFactory(ConfigurationAccessor configurationAccessor, Gson gson, AlertProperties alertProperties, ProxyManager proxyManager) {
        super(configurationAccessor);
        this.gson = gson;
        this.alertProperties = alertProperties;
        this.proxyManager = proxyManager;
    }

    @Override
    public BlackDuckProperties createProperties(Long blackDuckConfigId, FieldUtility fieldUtility) {
        return new BlackDuckProperties(blackDuckConfigId, gson, alertProperties, proxyManager, fieldUtility);
    }

    // TODO remove duplicate method
    public Optional<BlackDuckProperties> createPropertiesIfConfigExists(Long blackDuckConfigId) {
        return createProperties(blackDuckConfigId);
    }

}
