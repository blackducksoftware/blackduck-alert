package com.synopsys.integration.alert.api.channel.issue.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueTrackerModelHolder;
import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueTrackerResponse;
import com.synopsys.integration.alert.api.channel.issue.tracker.send.IssueTrackerAsyncMessageSender;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.processor.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.api.processor.extract.model.project.ProjectMessage;

class IssueTrackerProcessorTest {
    @Test
    void processMessagesTest() throws AlertException {
        IssueTrackerModelExtractor<String> extractor = Mockito.mock(IssueTrackerModelExtractor.class);
        IssueTrackerModelHolder<String> simpleMessageResponses = new IssueTrackerModelHolder<>(List.of(), List.of(), List.of());
        Mockito.when(extractor.extractSimpleMessageIssueModels(Mockito.anyList(), Mockito.any())).thenReturn(simpleMessageResponses);
        IssueTrackerModelHolder<String> projectMessageResponses = new IssueTrackerModelHolder<>(List.of(), List.of(), List.of());

        IssueTrackerAsyncMessageSender<String> sender = Mockito.mock(IssueTrackerAsyncMessageSender.class);
        AtomicInteger messageCounter = new AtomicInteger(0);
        Mockito.doAnswer(invocation -> {
            messageCounter.incrementAndGet();
            return null;
        }).when(sender).sendAsyncMessages(Mockito.any());

        IssueTrackerProcessor<String> processor = new IssueTrackerProcessor<>(extractor, sender);

        ProjectMessage projectMessage = Mockito.mock(ProjectMessage.class);
        List<ProjectMessage> projectMessages = List.of(projectMessage, projectMessage, projectMessage);
        ProviderMessageHolder providerMessageHolder = new ProviderMessageHolder(projectMessages, List.of());
        IssueTrackerResponse<String> issueTrackerResponse = processor.processMessages(providerMessageHolder, "jobName");
        assertEquals("Success", issueTrackerResponse.getStatusMessage());
        assertTrue(issueTrackerResponse.getUpdatedIssues().isEmpty());
    }

}
