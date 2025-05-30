/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor.distribute;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.descriptor.SlackChannelKey;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.api.processor.MockProcessingAuditAccessor;
import com.blackduck.integration.alert.api.processor.extract.model.ProcessedProviderMessage;
import com.blackduck.integration.alert.api.processor.extract.model.ProcessedProviderMessageHolder;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;
import com.blackduck.integration.alert.api.processor.extract.model.SimpleMessage;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectOperation;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

class ProviderMessageDistributorTest {
     private final UUID uuid = UUID.randomUUID();
     private final UUID jobExecutionId = UUID.randomUUID();
     private final SlackChannelKey slackChannelKey = new SlackChannelKey();

     @Test
     void distributeTest() {
         MockProcessingAuditAccessor processingAuditAccessor = new MockProcessingAuditAccessor();
         EventManager eventManager = Mockito.mock(EventManager.class);
         ExecutingJobManager executingJobManager = Mockito.mock(ExecutingJobManager.class);

         ProcessedNotificationDetails processedNotificationDetails = new ProcessedNotificationDetails(jobExecutionId, uuid, slackChannelKey.getUniversalKey(), "JobName");
         ProcessedProviderMessageHolder processedMessageHolder = createProcessedProviderMessageHolder(2, 2);

         ProviderMessageDistributor providerMessageDistributor = new ProviderMessageDistributor(processingAuditAccessor, eventManager, executingJobManager);
         providerMessageDistributor.distribute(processedNotificationDetails, processedMessageHolder);

         Mockito.verify(eventManager, Mockito.times(4)).sendEvent(Mockito.any());
    }

     @Test
     void distributeMissingDestinationKeyTest() {
         MockProcessingAuditAccessor processingAuditAccessor = new MockProcessingAuditAccessor();
         EventManager eventManager = Mockito.mock(EventManager.class);
         ExecutingJobManager executingJobManager = Mockito.mock(ExecutingJobManager.class);

         ProcessedNotificationDetails processedNotificationDetails = new ProcessedNotificationDetails(jobExecutionId, uuid, "bad channel key", "JobName");
         ProcessedProviderMessageHolder processedMessageHolder = createProcessedProviderMessageHolder(1, 0);

         ProviderMessageDistributor providerMessageDistributor = new ProviderMessageDistributor(processingAuditAccessor, eventManager, executingJobManager);
         providerMessageDistributor.distribute(processedNotificationDetails, processedMessageHolder);

         Mockito.verify(eventManager, Mockito.times(0)).sendEvent(Mockito.any());
    }

    private ProcessedProviderMessageHolder createProcessedProviderMessageHolder(int numberOfProjectMessages, int numberOfSimpleMessages) {
        List<ProcessedProviderMessage<ProjectMessage>> processedProjectMessages = new ArrayList<>();
        List<ProcessedProviderMessage<SimpleMessage>> processedSimpleMessages = new ArrayList<>();

        LinkableItem commonProject = new LinkableItem("Project", "Common Project");
        ProjectOperation commonOperation = ProjectOperation.CREATE;

        for (long i = 1; i < numberOfProjectMessages + 1; i++) {
            LinkableItem provider = new LinkableItem("Provider", "Provider " + i);
            ProviderDetails providerDetails = new ProviderDetails(i, provider);
            ProjectMessage projectMessage = ProjectMessage.projectStatusInfo(providerDetails, commonProject, commonOperation);

            ProcessedProviderMessage<ProjectMessage> processedProviderMessage = ProcessedProviderMessage.singleSource(i, projectMessage);
            processedProjectMessages.add(processedProviderMessage);
        }

        for (long i = 1; i < numberOfSimpleMessages + 1; i++) {
            LinkableItem provider = new LinkableItem("Details", "Detail " + i);
            ProviderDetails providerDetails = new ProviderDetails(i, provider);

            SimpleMessage simpleMessage = SimpleMessage.original(providerDetails, "Summary " + i, "Description " + i, List.of(commonProject));
            ProcessedProviderMessage<SimpleMessage> processedSimpleMessage = ProcessedProviderMessage.singleSource(i, simpleMessage);
            processedSimpleMessages.add(processedSimpleMessage);
        }

        return new ProcessedProviderMessageHolder(processedProjectMessages, processedSimpleMessages);
    }
}
