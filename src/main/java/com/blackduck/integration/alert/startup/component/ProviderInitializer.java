package com.blackduck.integration.alert.startup.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.provider.lifecycle.ProviderSchedulingManager;

@Component
@Order(60)
public class ProviderInitializer extends StartupComponent {
    private final ProviderSchedulingManager providerLifecycleManager;

    @Autowired
    public ProviderInitializer(ProviderSchedulingManager providerLifecycleManager) {
        this.providerLifecycleManager = providerLifecycleManager;
    }

    @Override
    protected void initialize() {
        providerLifecycleManager.initializeConfiguredProviders();
    }

}
