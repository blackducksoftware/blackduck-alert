package com.synopsys.integration.alert.channel.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.processor.api.distribute.DistributionEventV2;

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
        AuditAccessor auditAccessor = Mockito.mock(AuditAccessor.class);
        Mockito.doNothing().when(auditAccessor).setAuditEntrySuccess(Mockito.anyCollection());

        DistributionJobDetailsModel details = new DistributionJobDetailsModel(null, null) {};
        JobDetailsAccessor<DistributionJobDetailsModel> jobDetailsAccessor = x -> Optional.of(details);

        DistributionChannelV2<DistributionJobDetailsModel> channel = (x, y) -> null;

        DistributionEventReceiver<DistributionJobDetailsModel> receiver = new DistributionEventReceiver<>(null, auditAccessor, jobDetailsAccessor, channel, null) {};

        Long auditId = 0L;
        ChannelKey channelKey = new ChannelKey("test universal key", null);
        DistributionEventV2 testEvent = new DistributionEventV2(channelKey, null, auditId, null);
        receiver.handleEvent(testEvent);

        Mockito.verify(auditAccessor, Mockito.times(1)).setAuditEntrySuccess(Mockito.eq(Set.of(auditId)));
    }

    @Test
    public void handleEventExceptionTest() {
        AuditAccessor auditAccessor = Mockito.mock(AuditAccessor.class);
        Mockito.doNothing().when(auditAccessor).setAuditEntryFailure(Mockito.anyCollection(), Mockito.anyString(), Mockito.any());

        DistributionJobDetailsModel details = new DistributionJobDetailsModel(null, null) {};
        JobDetailsAccessor<DistributionJobDetailsModel> jobDetailsAccessor = x -> Optional.of(details);

        AlertException testException = new AlertException("Test exception");
        DistributionChannelV2<DistributionJobDetailsModel> channel = (x, y) -> {
            throw testException;
        };

        DistributionEventReceiver<DistributionJobDetailsModel> receiver = new DistributionEventReceiver<>(null, auditAccessor, jobDetailsAccessor, channel, null) {};

        Long auditId = 0L;
        ChannelKey channelKey = new ChannelKey("test universal key", null);
        DistributionEventV2 testEvent = new DistributionEventV2(channelKey, null, auditId, null);
        receiver.handleEvent(testEvent);

        Mockito.verify(auditAccessor, Mockito.times(1)).setAuditEntryFailure(Mockito.eq(Set.of(auditId)), Mockito.anyString(), Mockito.any());
    }

    @Test
    public void handleEventJobDetailsMissingTest() {
        AuditAccessor auditAccessor = Mockito.mock(AuditAccessor.class);
        Mockito.doNothing().when(auditAccessor).setAuditEntryFailure(Mockito.anyCollection(), Mockito.anyString(), Mockito.any());

        JobDetailsAccessor<DistributionJobDetailsModel> jobDetailsAccessor = x -> Optional.empty();
        DistributionEventReceiver<DistributionJobDetailsModel> receiver = new DistributionEventReceiver<>(null, auditAccessor, jobDetailsAccessor, null, null) {};

        Long auditId = 0L;
        ChannelKey channelKey = new ChannelKey("test universal key", null);
        DistributionEventV2 testEvent = new DistributionEventV2(channelKey, null, auditId, null);
        receiver.handleEvent(testEvent);

        Mockito.verify(auditAccessor, Mockito.times(1)).setAuditEntryFailure(Mockito.eq(Set.of(auditId)), Mockito.anyString(), Mockito.any());
    }

}
