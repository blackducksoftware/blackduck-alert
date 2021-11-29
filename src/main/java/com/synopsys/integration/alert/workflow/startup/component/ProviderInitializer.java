/*
 * blackduck-alert
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.workflow.startup.component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.activemq.broker.BrokerRegistry;
import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.provider.lifecycle.ProviderSchedulingManager;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.wait.WaitJob;
import com.synopsys.integration.wait.WaitJobTask;

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

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this::waitForBrokerServiceAndConfigure);
    }

    private void waitForBrokerServiceAndConfigure() {
        try {
            IntLogger intLogger = new Slf4jIntLogger(logger);
            long startTimestamp = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            WaitJob waitJob = WaitJob.create(intLogger, 600, startTimestamp, 10, "Active MQ Broker Service", new BrokerWaitTask());
            boolean isComplete = waitJob.waitFor();
            if (isComplete) {
                logger.info("Active MQ Broker Service found.  Configuring...");
                BrokerService brokerService = BrokerRegistry.getInstance().lookup(BrokerService.DEFAULT_BROKER_NAME);
                if (null != brokerService) {
                    providerLifecycleManager.initializeConfiguredProviders();
                }
            } else {
                logger.info("Active MQ Broker Service not found.");
            }
        } catch (IntegrationException ex) {
            logger.error("Error waiting for Active MQ broker service.", ex);
        } catch (InterruptedException ex) {
            Thread.interrupted();
            logger.error("Error waiting for Active MQ broker service.", ex);
        }
    }

    private class BrokerWaitTask implements WaitJobTask {
        @Override
        public boolean isComplete() {
            return BrokerRegistry.getInstance().lookup(BrokerService.DEFAULT_BROKER_NAME) != null;
        }
    }
}
