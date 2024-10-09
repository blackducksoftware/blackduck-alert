package com.blackduck.integration.alert.api.distribution;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import com.google.gson.Gson;
import com.blackduck.integration.alert.api.event.AlertEventHandler;
import com.blackduck.integration.alert.api.event.distribution.JobSubTaskEvent;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;

class JobSubTaskMessageListenerTest {

    @Test
    void testHandlerCalled() {
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        String destination = "destination";
        TestEventHandler handler = new TestEventHandler();
        TestEvent event = new TestEvent(destination, UUID.randomUUID(), UUID.randomUUID(), Set.of());
        TestEventListener listener = new TestEventListener(gson, new SyncTaskExecutor(), destination, TestEvent.class, handler);
        Message message = new Message(gson.toJson(event).getBytes());
        listener.onMessage(message);
        assertTrue(handler.wasHandlerCalled());
    }

    private static class TestEvent extends JobSubTaskEvent {
        private static final long serialVersionUID = -4316390923286571736L;

        public TestEvent(String destination, UUID jobExecutionId, UUID jobId, Set<Long> notificationIds) {
            super(destination, jobExecutionId, jobId, notificationIds);
        }
    }

    private static class TestEventListener extends JobSubTaskMessageListener<TestEvent> {
        public TestEventListener(
            Gson gson,
            TaskExecutor taskExecutor,
            String destinationName,
            Class<TestEvent> eventClass,
            AlertEventHandler<TestEvent> eventHandler
        ) {
            super(gson, taskExecutor, destinationName, eventClass, eventHandler);
        }
    }

    private static class TestEventHandler implements AlertEventHandler<TestEvent> {
        private boolean wasHandlerCalled = false;

        @Override
        public void handle(TestEvent event) {
            this.wasHandlerCalled = true;
        }

        public boolean wasHandlerCalled() {
            return wasHandlerCalled;
        }
    }
}
