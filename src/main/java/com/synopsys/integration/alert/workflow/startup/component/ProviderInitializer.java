package com.synopsys.integration.alert.workflow.startup.component;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.provider.Provider;

@Component
@Order(5)
public class ProviderInitializer extends StartupComponent {
    private final List<Provider> providers;

    @Autowired
    public ProviderInitializer(final List<Provider> providers) {
        this.providers = providers;
    }

    @Override
    public void run() {
        providers.forEach(Provider::initialize);
    }
}
