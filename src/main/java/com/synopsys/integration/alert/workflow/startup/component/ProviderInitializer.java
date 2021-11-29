/*
 * blackduck-alert
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.workflow.startup.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.provider.lifecycle.ProviderSchedulingManager;
import com.synopsys.integration.alert.configuration.BrokerServiceDependentTask;

@Component
@Order(60)
public class ProviderInitializer extends StartupComponent {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final ProviderSchedulingManager providerLifecycleManager;

    @Autowired
    public ProviderInitializer(ProviderSchedulingManager providerLifecycleManager) {
        this.providerLifecycleManager = providerLifecycleManager;
    }

    @Override
    protected void initialize() {
        BrokerServiceDependentTask task = new BrokerServiceDependentTask("Provider initialization", (brokerService) -> {
            brokerService.waitUntilStarted();
            providerLifecycleManager.initializeConfiguredProviders();
        });
        task.waitForServiceAndExecute();
    }
}
