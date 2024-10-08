/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.provider.lifecycle.ProviderPropertiesFactory;
import com.blackduck.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.google.gson.Gson;
import com.blackduck.integration.alert.common.AlertProperties;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.FieldUtility;
import com.blackduck.integration.alert.common.rest.proxy.ProxyManager;

@Component
public class BlackDuckPropertiesFactory extends ProviderPropertiesFactory<BlackDuckProperties> {
    private final Gson gson;
    private final AlertProperties alertProperties;
    private final ProxyManager proxyManager;

    @Autowired
    public BlackDuckPropertiesFactory(ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor, Gson gson, AlertProperties alertProperties, ProxyManager proxyManager) {
        super(configurationModelConfigurationAccessor);
        this.gson = gson;
        this.alertProperties = alertProperties;
        this.proxyManager = proxyManager;
    }

    @Override
    public BlackDuckProperties createProperties(Long blackDuckConfigId, FieldUtility fieldUtility) {
        return new BlackDuckProperties(blackDuckConfigId, gson, BlackDuckServicesFactory.createDefaultObjectMapper(), alertProperties, proxyManager, fieldUtility);
    }

}
