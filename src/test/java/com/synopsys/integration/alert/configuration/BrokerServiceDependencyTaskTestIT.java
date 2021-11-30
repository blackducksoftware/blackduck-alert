package com.synopsys.integration.alert.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
public class BrokerServiceDependencyTaskTestIT {

    @Test
    public void testWaitForBrokerService() throws InterruptedException {
        String taskName = "Test Broker Wait Task";
        AtomicBoolean notDone = new AtomicBoolean(true);
        BrokerServiceDependentTask task = new BrokerServiceDependentTask(taskName, (ignored) -> notDone.compareAndSet(true, false));
        task.waitForServiceAndExecute();
        while (notDone.get()) {
            Thread.sleep(100);
        }
        
        assertEquals(taskName, task.getTaskToExecuteName());
    }
}
