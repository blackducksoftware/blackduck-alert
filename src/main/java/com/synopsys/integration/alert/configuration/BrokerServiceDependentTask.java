package com.synopsys.integration.alert.configuration;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.apache.activemq.broker.BrokerRegistry;
import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.wait.WaitJob;
import com.synopsys.integration.wait.WaitJobTask;

public class BrokerServiceDependentTask {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private String taskToExecuteName;
    private Consumer<BrokerService> taskToExecuteOnceFound;

    public BrokerServiceDependentTask(String taskToExecuteName, Consumer<BrokerService> taskToExecuteOnceFound) {
        this.taskToExecuteName = taskToExecuteName;
        this.taskToExecuteOnceFound = taskToExecuteOnceFound;
    }

    public void waitForServiceAndExecute() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this::waitForBrokerServiceAndExecute);
    }

    private void waitForBrokerServiceAndExecute() {
        try {
            IntLogger intLogger = new Slf4jIntLogger(logger);
            long startTimestamp = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            WaitJob waitJob = WaitJob.create(intLogger, 600, startTimestamp, 10, "Active MQ Broker Service Lookup", new BrokerWaitTask());
            boolean isComplete = waitJob.waitFor();
            if (isComplete) {
                logger.info("Active MQ Broker Service found.  Executing task: {}", taskToExecuteName);
                BrokerService brokerService = BrokerRegistry.getInstance().lookup(BrokerService.DEFAULT_BROKER_NAME);
                if (null != brokerService) {
                    taskToExecuteOnceFound.accept(brokerService);
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
