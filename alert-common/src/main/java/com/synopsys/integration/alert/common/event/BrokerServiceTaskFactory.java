package com.synopsys.integration.alert.common.event;

import java.util.function.Consumer;

import org.apache.activemq.broker.BrokerService;
import org.springframework.stereotype.Component;

@Component
public class BrokerServiceTaskFactory {
    public static BrokerServiceWaitTask createDefaultWaitTask() {
        return new BrokerServiceWaitTask();
    }

    public BrokerServiceDependentTask createTask(String taskName, Consumer<BrokerService> task) {
        return createTask(taskName, createDefaultWaitTask(), task);
    }

    public BrokerServiceDependentTask createTask(String taskName, BrokerServiceWaitTask brokerServiceWaitTask, Consumer<BrokerService> task) {
        return new BrokerServiceDependentTask(taskName, brokerServiceWaitTask, task);
    }
}
