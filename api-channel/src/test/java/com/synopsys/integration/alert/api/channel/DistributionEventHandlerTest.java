package com.synopsys.integration.alert.api.channel;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.processor.api.distribute.DistributionEvent;

public class DistributionEventHandlerTest {
    @Test
    public void handleEventSuccessTest() {
        ProcessingAuditAccessor auditAccessor = Mockito.mock(ProcessingAuditAccessor.class);
        Mockito.doNothing().when(auditAccessor).setAuditEntrySuccess(Mockito.any(), Mockito.anySet());

        DistributionJobDetailsModel details = new DistributionJobDetailsModel(null, null) {};
        JobDetailsAccessor<DistributionJobDetailsModel> jobDetailsAccessor = x -> Optional.of(details);

        DistributionChannel<DistributionJobDetailsModel> channel = (x, y) -> null;

        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        DistributionJobModel distributionJobModel = Mockito.mock(DistributionJobModel.class);
        Mockito.when(distributionJobModel.getName()).thenReturn("jobName");
        Mockito.when(jobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(distributionJobModel));
        DistributionEventHandler<DistributionJobDetailsModel> eventHandler = new DistributionEventHandler<>(channel, jobDetailsAccessor, auditAccessor, jobAccessor);

        UUID testJobId = UUID.randomUUID();
        Set<Long> testNotificationIds = Set.of(1L, 3L, 5L);

        ChannelKey channelKey = new ChannelKey("test universal key", null);
        DistributionEvent testEvent = new DistributionEvent(channelKey, testJobId, testNotificationIds, null);
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
        DistributionChannel<DistributionJobDetailsModel> channel = (x, y) -> {
            throw testException;
        };

        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        DistributionJobModel distributionJobModel = Mockito.mock(DistributionJobModel.class);
        Mockito.when(distributionJobModel.getName()).thenReturn("jobName");
        Mockito.when(jobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(distributionJobModel));
        DistributionEventHandler<DistributionJobDetailsModel> eventHandler = new DistributionEventHandler<>(channel, jobDetailsAccessor, auditAccessor, jobAccessor);

        UUID testJobId = UUID.randomUUID();
        Set<Long> testNotificationIds = Set.of(1L, 3L, 5L);

        ChannelKey channelKey = new ChannelKey("test universal key", null);
        DistributionEvent testEvent = new DistributionEvent(channelKey, testJobId, testNotificationIds, null);
        eventHandler.handle(testEvent);

        Mockito.verify(auditAccessor, Mockito.times(1)).setAuditEntryFailure(Mockito.eq(testJobId), Mockito.eq(testNotificationIds), Mockito.anyString(), Mockito.any());
    }

    @Test
    public void handleEventJobDetailsMissingTest() {
        ProcessingAuditAccessor auditAccessor = Mockito.mock(ProcessingAuditAccessor.class);
        Mockito.doNothing().when(auditAccessor).setAuditEntryFailure(Mockito.any(), Mockito.anySet(), Mockito.anyString(), Mockito.any());

        JobDetailsAccessor<DistributionJobDetailsModel> jobDetailsAccessor = x -> Optional.empty();
        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        DistributionJobModel distributionJobModel = Mockito.mock(DistributionJobModel.class);
        Mockito.when(distributionJobModel.getName()).thenReturn("jobName");
        Mockito.when(jobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(distributionJobModel));
        DistributionEventHandler<DistributionJobDetailsModel> eventHandler = new DistributionEventHandler<>(null, jobDetailsAccessor, auditAccessor, jobAccessor);

        UUID testJobId = UUID.randomUUID();
        Set<Long> testNotificationIds = Set.of(1L, 3L, 5L);

        ChannelKey channelKey = new ChannelKey("test universal key", null);
        DistributionEvent testEvent = new DistributionEvent(channelKey, testJobId, testNotificationIds, null);
        eventHandler.handle(testEvent);

        Mockito.verify(auditAccessor, Mockito.times(1)).setAuditEntryFailure(Mockito.eq(testJobId), Mockito.eq(testNotificationIds), Mockito.anyString(), Mockito.any());
    }

}
