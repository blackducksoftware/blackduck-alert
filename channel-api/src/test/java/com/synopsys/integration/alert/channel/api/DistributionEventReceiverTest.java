package com.synopsys.integration.alert.channel.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.processor.api.distribute.DistributionEvent;

public class DistributionEventReceiverTest {
    @Test
    public void getDestinationTest() {
        ChannelKey channelKey = new ChannelKey("test universal key", null);
        DistributionEventReceiver<DistributionJobDetailsModel> receiver = new DistributionEventReceiver<>(null, null, null, null, channelKey) {};

        String destinationName = receiver.getDestinationName();
        assertEquals(channelKey.getUniversalKey(), destinationName);
    }

    @Test
    public void handleEventSuccessTest() {
        ProcessingAuditAccessor auditAccessor = Mockito.mock(ProcessingAuditAccessor.class);
        Mockito.doNothing().when(auditAccessor).setAuditEntrySuccess(Mockito.any(), Mockito.anySet());

        DistributionJobDetailsModel details = new DistributionJobDetailsModel(null, null) {};
        JobDetailsAccessor<DistributionJobDetailsModel> jobDetailsAccessor = x -> Optional.of(details);

        DistributionChannel<DistributionJobDetailsModel> channel = (x, y) -> null;

        DistributionEventReceiver<DistributionJobDetailsModel> receiver = new DistributionEventReceiver<>(null, auditAccessor, jobDetailsAccessor, channel, null) {};

        UUID testJobId = UUID.randomUUID();
        Set<Long> testNotificationIds = Set.of(1L, 3L, 5L);

        ChannelKey channelKey = new ChannelKey("test universal key", null);
        DistributionEvent testEvent = new DistributionEvent(channelKey, testJobId, testNotificationIds, null);
        receiver.handleEvent(testEvent);

        Mockito.verify(auditAccessor, Mockito.times(1)).setAuditEntrySuccess(Mockito.eq(testJobId), Mockito.eq(testNotificationIds));
    }

    @Test
    public void handleEventExceptionTest() {
        ProcessingAuditAccessor auditAccessor = Mockito.mock(ProcessingAuditAccessor.class);
        Mockito.doNothing().when(auditAccessor).setAuditEntryFailure(Mockito.anyCollection(), Mockito.anyString(), Mockito.any());

        DistributionJobDetailsModel details = new DistributionJobDetailsModel(null, null) {};
        JobDetailsAccessor<DistributionJobDetailsModel> jobDetailsAccessor = x -> Optional.of(details);

        AlertException testException = new AlertException("Test exception");
        DistributionChannel<DistributionJobDetailsModel> channel = (x, y) -> {
            throw testException;
        };

        DistributionEventReceiver<DistributionJobDetailsModel> receiver = new DistributionEventReceiver<>(null, auditAccessor, jobDetailsAccessor, channel, null) {};

        UUID testJobId = UUID.randomUUID();
        Set<Long> testNotificationIds = Set.of(1L, 3L, 5L);

        ChannelKey channelKey = new ChannelKey("test universal key", null);
        DistributionEvent testEvent = new DistributionEvent(channelKey, testJobId, testNotificationIds, null);
        receiver.handleEvent(testEvent);

        Mockito.verify(auditAccessor, Mockito.times(1)).setAuditEntryFailure(Mockito.eq(testJobId), Mockito.eq(testNotificationIds), Mockito.anyString(), Mockito.any());
    }

    @Test
    public void handleEventJobDetailsMissingTest() {
        ProcessingAuditAccessor auditAccessor = Mockito.mock(ProcessingAuditAccessor.class);
        Mockito.doNothing().when(auditAccessor).setAuditEntryFailure(Mockito.anyCollection(), Mockito.anyString(), Mockito.any());

        JobDetailsAccessor<DistributionJobDetailsModel> jobDetailsAccessor = x -> Optional.empty();
        DistributionEventReceiver<DistributionJobDetailsModel> receiver = new DistributionEventReceiver<>(null, auditAccessor, jobDetailsAccessor, null, null) {};

        UUID testJobId = UUID.randomUUID();
        Set<Long> testNotificationIds = Set.of(1L, 3L, 5L);

        ChannelKey channelKey = new ChannelKey("test universal key", null);
        DistributionEvent testEvent = new DistributionEvent(channelKey, testJobId, testNotificationIds, null);
        receiver.handleEvent(testEvent);

        Mockito.verify(auditAccessor, Mockito.times(1)).setAuditEntryFailure(Mockito.eq(testJobId), Mockito.eq(testNotificationIds), Mockito.anyString(), Mockito.any());
    }

}
