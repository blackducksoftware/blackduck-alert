/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.provider.notification.ProviderDistributionFilter;
import com.synopsys.integration.alert.common.provider.notification.ProviderNotificationClassMap;
import com.synopsys.integration.alert.provider.blackduck.filter.BlackDuckDistributionFilter;
import com.synopsys.integration.alert.provider.blackduck.filter.BlackDuckProjectNameExtractor;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;

@Component
public class DistributionFilterFactory {
    private final Gson gson;

    @Autowired
    public DistributionFilterFactory(Gson gson) {
        this.gson = gson;
    }

    public ProviderDistributionFilter createFilter(BlackDuckServicesFactory blackDuckServicesFactory, ProviderNotificationClassMap providerNotificationClassMap) {
        BlackDuckProjectNameExtractor nameExtractor = new BlackDuckProjectNameExtractor(blackDuckServicesFactory);
        return new BlackDuckDistributionFilter(gson, providerNotificationClassMap, nameExtractor);
    }
}
