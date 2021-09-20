package com.synopsys.integration.alert.processor.api.distribute;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.descriptor.api.SlackChannelKey;
import com.synopsys.integration.alert.processor.api.MockProcessingAuditAccessor;
import com.synopsys.integration.alert.processor.api.extract.model.ProcessedProviderMessage;
import com.synopsys.integration.alert.processor.api.extract.model.ProcessedProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectOperation;

public class ProviderMessageDistributorTest {
    private final UUID uuid = UUID.randomUUID();
    private final SlackChannelKey slackChannelKey = new SlackChannelKey();

    @Test
    public void distributeTest() {
        MockProcessingAuditAccessor processingAuditAccessor = new MockProcessingAuditAccessor();
        EventManager eventManager = Mockito.mock(EventManager.class);

        ProcessedNotificationDetails processedNotificationDetails = new ProcessedNotificationDetails(uuid, slackChannelKey.getUniversalKey(), "JobName");
        ProcessedProviderMessageHolder processedMessageHolder = createProcessedProviderMessageHolder(2, 2);

        ProviderMessageDistributor providerMessageDistributor = new ProviderMessageDistributor(processingAuditAccessor, eventManager);
        providerMessageDistributor.distribute(processedNotificationDetails, processedMessageHolder);

        Mockito.verify(eventManager, Mockito.times(4)).sendEvent(Mockito.any());
    }

    @Test
    public void distributeMissingDestinationKeyTest() {
        MockProcessingAuditAccessor processingAuditAccessor = new MockProcessingAuditAccessor();
        EventManager eventManager = Mockito.mock(EventManager.class);

        ProcessedNotificationDetails processedNotificationDetails = new ProcessedNotificationDetails(uuid, "bad channel key", "JobName");
        ProcessedProviderMessageHolder processedMessageHolder = createProcessedProviderMessageHolder(1, 0);

        ProviderMessageDistributor providerMessageDistributor = new ProviderMessageDistributor(processingAuditAccessor, eventManager);
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
