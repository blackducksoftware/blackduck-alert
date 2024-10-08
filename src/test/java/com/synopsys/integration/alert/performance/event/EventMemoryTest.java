package com.synopsys.integration.alert.performance.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;

import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.configuration.ApplicationConfiguration;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.blackduck.integration.alert.test.common.TestTags;
import com.synopsys.integration.alert.util.DescriptorMocker;

@Tag(TestTags.DEFAULT_PERFORMANCE)
@SpringBootTest
@ContextConfiguration(classes = { Application.class, ApplicationConfiguration.class, DatabaseDataSource.class, DescriptorMocker.class, TestEventListenerConfiguration.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@WebAppConfiguration
class EventMemoryTest {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private EventManager eventManager;
    @Autowired
    private List<TestAlertEventListener> eventListeners;

    @Test
    @Ignore // performance test
    @Disabled
    void testEventLoadTest() {
        executeTest();
    }

    private void executeTest() {
        int count = 1000000;
        Instant start = Instant.now();
        for (int index = 0; index < count; index++) {
            for (TestAlertEventListener listener : eventListeners) {
                eventManager.sendEvent(new TestAlertEvent(listener.getDestinationName(), createEventContent()));
            }
        }
        try {
            boolean notDone = true;
            while (notDone) {
                notDone = eventListeners.stream()
                    .anyMatch(listener -> count != listener.getHandler().getMessageCount());
                if (notDone) {
                    // 1 minute between polls
                    Thread.sleep(60000);
                }
            }
        } catch (InterruptedException ex) {
            Thread.interrupted();
        }
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        logger.info("Execution duration {}h:{}m:{}s.{}ms", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart(), duration.toMillisPart());
        for (TestAlertEventListener eventListener : eventListeners) {
            assertEquals(count, eventListener.getHandler().getMessageCount());
        }
    }

    private String createEventContent() {
        StringBuilder builder = new StringBuilder(10000);
        builder.append("testtext12".repeat(999));
        return builder.toString();
    }
}
