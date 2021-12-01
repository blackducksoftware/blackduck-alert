/*
 * blackduck-alert
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.workflow.startup.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.event.BrokerServiceDependentTask;
import com.synopsys.integration.alert.common.event.BrokerServiceTaskFactory;
import com.synopsys.integration.alert.common.provider.lifecycle.ProviderSchedulingManager;

@Component
@Order(60)
public class ProviderInitializer extends StartupComponent {
    private final ProviderSchedulingManager providerLifecycleManager;
    private final BrokerServiceTaskFactory brokerServiceTaskFactory;

    @Autowired
    public ProviderInitializer(ProviderSchedulingManager providerLifecycleManager, BrokerServiceTaskFactory brokerServiceTaskFactory) {
        this.providerLifecycleManager = providerLifecycleManager;
        this.brokerServiceTaskFactory = brokerServiceTaskFactory;
    }


    @Override
    protected void initialize() {
        BrokerServiceDependentTask task = brokerServiceTaskFactory.createTask("Provider initialization", (brokerService) -> {
            brokerService.waitUntilStarted();
            providerLifecycleManager.initializeConfiguredProviders();
        });
        task.waitForServiceAndExecute();
    }
}
