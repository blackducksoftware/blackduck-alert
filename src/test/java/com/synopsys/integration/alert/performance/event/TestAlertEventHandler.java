package com.synopsys.integration.alert.performance.event;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.api.event.AlertEventHandler;

public class TestAlertEventHandler implements AlertEventHandler<TestAlertEvent> {
    public static final int LOGGING_COUNT_THRESHOLD = 100000;
    public static final String DESTINATION_NAME = "alert_memory_test_destination";
    private Logger logger = LoggerFactory.getLogger(getClass());
    private AtomicLong messageCount;

    public TestAlertEventHandler() {
        this.messageCount = new AtomicLong();
    }

    public long getMessageCount() {
        return messageCount.get();
    }

    @Override
    public void handle(TestAlertEvent event) {
        long count = messageCount.incrementAndGet();
        if (count % LOGGING_COUNT_THRESHOLD == 0) {
            logger.info("Consumer called {}", count);
        }
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.interrupted();
            e.printStackTrace();
        }
    }
}
