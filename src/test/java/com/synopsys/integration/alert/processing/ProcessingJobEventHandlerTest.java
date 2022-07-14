package com.synopsys.integration.alert.processing;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobNotificationMappingAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.processor.api.NotificationContentProcessor;
import com.synopsys.integration.alert.processor.api.NotificationProcessingLifecycleCache;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractionDelegator;
import com.synopsys.integration.alert.processor.api.distribute.ProviderMessageDistributor;
import com.synopsys.integration.alert.processor.api.event.JobProcessingEvent;
import com.synopsys.integration.alert.telemetry.database.TelemetryAccessor;

class ProcessingJobEventHandlerTest {
    @Test
    void handleEventTest() {
        UUID correlationId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        NotificationDetailExtractionDelegator notificationDetailExtractionDelegator = Mockito.mock(NotificationDetailExtractionDelegator.class);
        NotificationContentProcessor notificationContentProcessor = Mockito.mock(NotificationContentProcessor.class);
        ProviderMessageDistributor providerMessageDistributor = Mockito.mock(ProviderMessageDistributor.class);
        List<NotificationProcessingLifecycleCache> lifecycleCaches = List.of();
        NotificationAccessor notificationAccessor = Mockito.mock(NotificationAccessor.class);
        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        JobNotificationMappingAccessor jobNotificationMappingAccessor = Mockito.mock(JobNotificationMappingAccessor.class);
        TelemetryAccessor telemetryAccessor = Mockito.mock(TelemetryAccessor.class);
        ProcessingJobEventHandler eventHandler = new ProcessingJobEventHandler(
            notificationDetailExtractionDelegator,
            notificationContentProcessor,
            providerMessageDistributor,
            lifecycleCaches,
            notificationAccessor,
            jobAccessor,
            jobNotificationMappingAccessor,
            telemetryAccessor
        );
        try {
            eventHandler.handle(new JobProcessingEvent(correlationId, jobId));
        } catch (RuntimeException e) {
            fail("Unable to handle event", e);
        }
    }
}
