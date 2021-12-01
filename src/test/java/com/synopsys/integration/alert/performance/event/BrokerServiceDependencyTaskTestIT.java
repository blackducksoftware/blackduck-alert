package com.synopsys.integration.alert.performance.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.event.BrokerServiceDependentTask;
import com.synopsys.integration.alert.common.event.BrokerServiceTaskFactory;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
public class BrokerServiceDependencyTaskTestIT {

    @Test
    public void testWaitForBrokerService() throws InterruptedException {
        String taskName = "Test Broker Wait Task";
        AtomicBoolean notDone = new AtomicBoolean(true);
        BrokerServiceTaskFactory taskFactory = new BrokerServiceTaskFactory();
        BrokerServiceDependentTask task = taskFactory.createTask(taskName, (ignored) -> notDone.compareAndSet(true, false));
        task.waitForServiceAndExecute();
        while (notDone.get()) {
            Thread.sleep(100);
        }

        assertEquals(taskName, task.getTaskToExecuteName());
    }
}
