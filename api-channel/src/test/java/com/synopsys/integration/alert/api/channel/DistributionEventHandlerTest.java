package com.synopsys.integration.alert.api.channel;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.processor.api.distribute.DistributionEvent;

public class DistributionEventHandlerTest {
    private final ChannelKey channelKey = new ChannelKey("test universal key", "Test Universal Key");

    @Test
    public void handleEventSuccessTest() {
        ProcessingAuditAccessor auditAccessor = Mockito.mock(ProcessingAuditAccessor.class);
        Mockito.doNothing().when(auditAccessor).setAuditEntrySuccess(Mockito.any(), Mockito.anySet());

        DistributionJobDetailsModel details = new DistributionJobDetailsModel(null, null) {};
        JobDetailsAccessor<DistributionJobDetailsModel> jobDetailsAccessor = x -> Optional.of(details);

        DistributionChannel<DistributionJobDetailsModel> channel = (x, y, z) -> null;

        DistributionEventHandler<DistributionJobDetailsModel> eventHandler = new DistributionEventHandler<>(channel, jobDetailsAccessor, auditAccessor);

        UUID testJobId = UUID.randomUUID();
        Set<Long> testNotificationIds = Set.of(1L, 3L, 5L);

        DistributionEvent testEvent = new DistributionEvent(channelKey, testJobId, "jobName", testNotificationIds, null);
        eventHandler.handle(testEvent);

        Mockito.verify(auditAccessor, Mockito.times(1)).setAuditEntrySuccess(Mockito.eq(testJobId), Mockito.eq(testNotificationIds));
    }

    @Test
    public void handleEventExceptionTest() {
        ProcessingAuditAccessor auditAccessor = Mockito.mock(ProcessingAuditAccessor.class);
        Mockito.doNothing().when(auditAccessor).setAuditEntryFailure(Mockito.any(), Mockito.anySet(), Mockito.anyString(), Mockito.any());

        DistributionJobDetailsModel details = new DistributionJobDetailsModel(null, null) {};
        JobDetailsAccessor<DistributionJobDetailsModel> jobDetailsAccessor = x -> Optional.of(details);

        AlertException testException = new AlertException("Test exception");
        DistributionChannel<DistributionJobDetailsModel> channel = (x, y, z) -> {
            throw testException;
        };

        DistributionEventHandler<DistributionJobDetailsModel> eventHandler = new DistributionEventHandler<>(channel, jobDetailsAccessor, auditAccessor);

        UUID testJobId = UUID.randomUUID();
        Set<Long> testNotificationIds = Set.of(1L, 3L, 5L);

        DistributionEvent testEvent = new DistributionEvent(channelKey, testJobId, "jobName", testNotificationIds, null);
        eventHandler.handle(testEvent);

        Mockito.verify(auditAccessor, Mockito.times(1)).setAuditEntryFailure(Mockito.eq(testJobId), Mockito.eq(testNotificationIds), Mockito.anyString(), Mockito.any());
    }

    @Test
    public void handleEventJobDetailsMissingTest() {
        ProcessingAuditAccessor auditAccessor = Mockito.mock(ProcessingAuditAccessor.class);
        Mockito.doNothing().when(auditAccessor).setAuditEntryFailure(Mockito.any(), Mockito.anySet(), Mockito.anyString(), Mockito.any());

        JobDetailsAccessor<DistributionJobDetailsModel> jobDetailsAccessor = x -> Optional.empty();
        DistributionEventHandler<DistributionJobDetailsModel> eventHandler = new DistributionEventHandler<>(null, jobDetailsAccessor, auditAccessor);

        UUID testJobId = UUID.randomUUID();
        Set<Long> testNotificationIds = Set.of(1L, 3L, 5L);

        DistributionEvent testEvent = new DistributionEvent(channelKey, testJobId, "jobName", testNotificationIds, null);
        eventHandler.handle(testEvent);

        Mockito.verify(auditAccessor, Mockito.times(1)).setAuditEntryFailure(Mockito.eq(testJobId), Mockito.eq(testNotificationIds), Mockito.anyString(), Mockito.any());
    }

}
