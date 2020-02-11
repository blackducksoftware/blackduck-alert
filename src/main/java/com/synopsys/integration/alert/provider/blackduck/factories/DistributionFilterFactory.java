package com.synopsys.integration.alert.provider.blackduck.factories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.provider.notification.ProviderDistributionFilter;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.filter.BlackDuckDistributionFilter;
import com.synopsys.integration.alert.provider.blackduck.filter.BlackDuckProjectNameExtractor;

@Component
public class DistributionFilterFactory {

    private final Gson gson;

    @Autowired
    public DistributionFilterFactory(Gson gson, BlackDuckProvider blackDuckProvider) {
        this.gson = gson;
        this.blackDuckProvider = blackDuckProvider;
    }

    public ProviderDistributionFilter createFilter(BlackDuckProperties providerProperties) {
        BlackDuckProjectNameExtractor nameExtractor = new BlackDuckProjectNameExtractor(providerProperties);
        return new BlackDuckDistributionFilter(gson, blackDuckProvider, nameExtractor);
    }
}
