package com.blackduck.integration.alert.processing;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.processor.JobNotificationContentProcessor;
import com.blackduck.integration.alert.api.processor.NotificationProcessingLifecycleCache;
import com.blackduck.integration.alert.api.processor.detail.NotificationDetailExtractionDelegator;
import com.blackduck.integration.alert.api.processor.digest.ProjectMessageDigester;
import com.blackduck.integration.alert.api.processor.distribute.ProviderMessageDistributor;
import com.blackduck.integration.alert.api.processor.event.JobProcessingEvent;
import com.blackduck.integration.alert.api.processor.extract.ProviderMessageExtractionDelegator;
import com.blackduck.integration.alert.api.processor.summarize.ProjectMessageSummarizer;
import com.blackduck.integration.alert.common.persistence.accessor.JobAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.JobCompletionStatusModelAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.JobNotificationMappingAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.blackduck.integration.alert.processing.ProcessingJobEventHandler;

class ProcessingJobEventHandlerTest {
    @Test
    void handleEventTest() {
        UUID correlationId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        NotificationDetailExtractionDelegator notificationDetailExtractionDelegator = Mockito.mock(NotificationDetailExtractionDelegator.class);
        ProviderMessageDistributor providerMessageDistributor = Mockito.mock(ProviderMessageDistributor.class);
        List<NotificationProcessingLifecycleCache> lifecycleCaches = List.of();
        NotificationAccessor notificationAccessor = Mockito.mock(NotificationAccessor.class);
        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        JobNotificationMappingAccessor jobNotificationMappingAccessor = Mockito.mock(JobNotificationMappingAccessor.class);
        ProviderMessageExtractionDelegator providerMessageExtractionDelegator = Mockito.mock(ProviderMessageExtractionDelegator.class);
        JobCompletionStatusModelAccessor jobCompletionStatusModelAccessor = Mockito.mock(JobCompletionStatusModelAccessor.class);
        ExecutingJobManager executingJobManager = new ExecutingJobManager(jobCompletionStatusModelAccessor);
        JobNotificationContentProcessor jobNotificationContentProcessor = new JobNotificationContentProcessor(
            notificationDetailExtractionDelegator,
            notificationAccessor,
            jobNotificationMappingAccessor,
            providerMessageExtractionDelegator,
            new ProjectMessageDigester(),
            new ProjectMessageSummarizer(),
            executingJobManager
        );

        ProcessingJobEventHandler eventHandler = new ProcessingJobEventHandler(
            providerMessageDistributor,
            lifecycleCaches,
            jobAccessor,
            jobNotificationMappingAccessor,
            jobNotificationContentProcessor,
            executingJobManager
        );
        try {
            eventHandler.handle(new JobProcessingEvent(correlationId, jobId));
        } catch (RuntimeException e) {
            fail("Unable to handle event", e);
        }
    }
}
