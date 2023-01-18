package com.synopsys.integration.alert.processing;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobNotificationMappingAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.processor.api.JobNotificationContentProcessor;
import com.synopsys.integration.alert.processor.api.NotificationContentProcessor;
import com.synopsys.integration.alert.processor.api.NotificationProcessingLifecycleCache;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractionDelegator;
import com.synopsys.integration.alert.processor.api.digest.ProjectMessageDigester;
import com.synopsys.integration.alert.processor.api.distribute.ProviderMessageDistributor;
import com.synopsys.integration.alert.processor.api.event.JobProcessingEvent;
import com.synopsys.integration.alert.processor.api.extract.ProviderMessageExtractionDelegator;
import com.synopsys.integration.alert.processor.api.summarize.ProjectMessageSummarizer;

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
        ProviderMessageExtractionDelegator providerMessageExtractionDelegator = Mockito.mock(ProviderMessageExtractionDelegator.class);
        JobNotificationContentProcessor jobNotificationContentProcessor = new JobNotificationContentProcessor(
            notificationDetailExtractionDelegator,
            notificationAccessor,
            jobAccessor,
            jobNotificationMappingAccessor,
            providerMessageExtractionDelegator,
            new ProjectMessageDigester(),
            new ProjectMessageSummarizer()
        );
        ProcessingJobEventHandler eventHandler = new ProcessingJobEventHandler(
            notificationDetailExtractionDelegator,
            notificationContentProcessor,
            providerMessageDistributor,
            lifecycleCaches,
            notificationAccessor,
            jobAccessor,
            jobNotificationMappingAccessor,
            jobNotificationContentProcessor
        );
        try {
            eventHandler.handle(new JobProcessingEvent(correlationId, jobId));
        } catch (RuntimeException e) {
            fail("Unable to handle event", e);
        }
    }
}
