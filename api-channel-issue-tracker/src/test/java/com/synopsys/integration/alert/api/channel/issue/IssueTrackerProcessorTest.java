package com.synopsys.integration.alert.api.channel.issue;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerModelHolder;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerResponse;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerMessageSender;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

public class IssueTrackerProcessorTest {
    @Test
    public void processMessagesTest() throws AlertException {
        IssueTrackerModelExtractor<String> extractor = Mockito.mock(IssueTrackerModelExtractor.class);
        IssueTrackerModelHolder<String> simpleMessageResponses = new IssueTrackerModelHolder<>(List.of(), List.of(), List.of());
        Mockito.when(extractor.extractSimpleMessageIssueModels(Mockito.anyList(), Mockito.any())).thenReturn(simpleMessageResponses);

        IssueTrackerIssueResponseModel<String> response1 = Mockito.mock(IssueTrackerIssueResponseModel.class);
        IssueTrackerIssueResponseModel<String> response2 = Mockito.mock(IssueTrackerIssueResponseModel.class);
        IssueTrackerIssueResponseModel<String> response3 = Mockito.mock(IssueTrackerIssueResponseModel.class);
        List<IssueTrackerIssueResponseModel<String>> simpleMessageResponse = List.of(response1, response2, response3);

        IssueTrackerIssueResponseModel<String> response4 = Mockito.mock(IssueTrackerIssueResponseModel.class);
        IssueTrackerIssueResponseModel<String> response5 = Mockito.mock(IssueTrackerIssueResponseModel.class);
        List<IssueTrackerIssueResponseModel<String>> projectMessageResponses = List.of(response4, response5);

        IssueTrackerMessageSender<String> sender = Mockito.mock(IssueTrackerMessageSender.class);
        Mockito.when(sender.sendMessages(Mockito.any())).thenReturn(
            simpleMessageResponse,
            projectMessageResponses
        );

        IssueTrackerProcessor<String> processor = new IssueTrackerProcessor<>(extractor, sender);

        ProjectMessage projectMessage = Mockito.mock(ProjectMessage.class);
        ProviderMessageHolder providerMessageHolder = new ProviderMessageHolder(List.of(projectMessage), List.of());
        IssueTrackerResponse<String> issueTrackerResponse = processor.processMessages(providerMessageHolder, "jobName");
        assertEquals(
            simpleMessageResponse.size() + projectMessageResponses.size(),
            issueTrackerResponse.getUpdatedIssues().size()
        );
    }

}
