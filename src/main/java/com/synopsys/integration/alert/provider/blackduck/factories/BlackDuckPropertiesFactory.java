package com.synopsys.integration.alert.provider.blackduck.factories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.provider.lifecycle.ProviderPropertiesFactory;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;

@Component
public class BlackDuckPropertiesFactory extends ProviderPropertiesFactory<BlackDuckProperties> {
    private final Gson gson;
    private final AlertProperties alertProperties;
    private final ProxyManager proxyManager;

    @Autowired
    public BlackDuckPropertiesFactory(Gson gson, AlertProperties alertProperties, ProxyManager proxyManager) {
        this.gson = gson;
        this.alertProperties = alertProperties;
        this.proxyManager = proxyManager;
    }

    @Override
    public BlackDuckProperties createProperties(Long configId, FieldAccessor fieldAccessor) {
        return new BlackDuckProperties(configId, gson, alertProperties, proxyManager, fieldAccessor);
    }
}
