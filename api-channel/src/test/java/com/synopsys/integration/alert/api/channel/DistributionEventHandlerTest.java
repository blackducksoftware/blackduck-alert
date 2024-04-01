package com.synopsys.integration.alert.api.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.api.descriptor.model.ChannelKey;
import com.synopsys.integration.alert.processor.api.distribute.DistributionEvent;

class DistributionEventHandlerTest {
    private final ChannelKey channelKey = new ChannelKey("test universal key", "Test Universal Key");

    @Test
    void handleEventSuccessTest() {
        AtomicInteger count = new AtomicInteger(0);

        EventManager eventManager = Mockito.mock(EventManager.class);
        DistributionJobDetailsModel details = new DistributionJobDetailsModel(null, null) {};
        JobDetailsAccessor<DistributionJobDetailsModel> jobDetailsAccessor = x -> Optional.of(details);

        DistributionChannel<DistributionJobDetailsModel> channel = (u, v, w, x, y, z) -> {
            count.incrementAndGet();
            return null;
        };

        DistributionEventHandler<DistributionJobDetailsModel> eventHandler = new DistributionEventHandler<>(channel, jobDetailsAccessor, eventManager);

        UUID testJobId = UUID.randomUUID();
        UUID jobExecutionId = UUID.randomUUID();
        Set<Long> testNotificationIds = Set.of(1L, 3L, 5L);

        DistributionEvent testEvent = new DistributionEvent(channelKey, testJobId, jobExecutionId, "jobName", testNotificationIds, null);
        eventHandler.handle(testEvent);

        assertEquals(1, count.get());
    }

    @Test
    void handleEventExceptionTest() {
        AtomicInteger count = new AtomicInteger(0);
        EventManager eventManager = Mockito.mock(EventManager.class);
        DistributionJobDetailsModel details = new DistributionJobDetailsModel(null, null) {};
        JobDetailsAccessor<DistributionJobDetailsModel> jobDetailsAccessor = x -> Optional.of(details);

        AlertException testException = new AlertException("Test exception");
        DistributionChannel<DistributionJobDetailsModel> channel = (u, v, w, x, y, z) -> {
            count.incrementAndGet();
            throw testException;
        };

        DistributionEventHandler<DistributionJobDetailsModel> eventHandler = new DistributionEventHandler<>(channel, jobDetailsAccessor, eventManager);

        UUID testJobId = UUID.randomUUID();
        UUID jobExecutionId = UUID.randomUUID();
        Set<Long> testNotificationIds = Set.of(1L, 3L, 5L);

        DistributionEvent testEvent = new DistributionEvent(channelKey, testJobId, jobExecutionId, "jobName", testNotificationIds, null);
        eventHandler.handle(testEvent);

        assertEquals(1, count.get());
    }

    @Test
    void handleEventJobDetailsMissingTest() {
        AtomicInteger count = new AtomicInteger(0);
        EventManager eventManager = Mockito.mock(EventManager.class);
        JobDetailsAccessor<DistributionJobDetailsModel> jobDetailsAccessor = x -> Optional.empty();
        DistributionChannel<DistributionJobDetailsModel> channel = (u, v, w, x, y, z) -> {
            count.incrementAndGet();
            return null;
        };
        DistributionEventHandler<DistributionJobDetailsModel> eventHandler = new DistributionEventHandler<>(channel, jobDetailsAccessor, eventManager);

        UUID testJobId = UUID.randomUUID();
        UUID jobExecutionId = UUID.randomUUID();
        Set<Long> testNotificationIds = Set.of(1L, 3L, 5L);

        DistributionEvent testEvent = new DistributionEvent(channelKey, testJobId, jobExecutionId, "jobName", testNotificationIds, null);
        eventHandler.handle(testEvent);

        assertEquals(0, count.get());
    }

}
