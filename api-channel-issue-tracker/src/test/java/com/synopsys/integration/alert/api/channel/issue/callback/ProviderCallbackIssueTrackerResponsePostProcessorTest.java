package com.synopsys.integration.alert.api.channel.issue.callback;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jms.core.JmsTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerResponse;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerCallbackInfo;

public class ProviderCallbackIssueTrackerResponsePostProcessorTest {
    private static final Gson GSON = new GsonBuilder().create();
    private static final IssueTrackerIssueResponseModel<String> ISSUE_RESPONSE_MODEL = new IssueTrackerIssueResponseModel<>(
        "issue-id",
        "issue-key",
        "https://issue-link",
        "Issue Title",
        IssueOperation.OPEN,
        new IssueTrackerCallbackInfo(0L, "https://callback-info", "https://project-version-url")
    );

    private static JmsTemplate MOCK_JMS_TEMPLATE;
    private static EventManager EVENT_MANAGER;

    @BeforeAll
    public static void init() {
        MOCK_JMS_TEMPLATE = Mockito.mock(JmsTemplate.class);
        Mockito.doNothing().when(MOCK_JMS_TEMPLATE).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
        EVENT_MANAGER = new EventManager(GSON, MOCK_JMS_TEMPLATE);
    }

    @Test
    public void postProcessNoResultsTest() {
        ProviderCallbackIssueTrackerResponsePostProcessor postProcessor = new ProviderCallbackIssueTrackerResponsePostProcessor(EVENT_MANAGER);

        IssueTrackerResponse<String> emptyResponse = new IssueTrackerResponse<>("Example Status Message", List.of());
        postProcessor.postProcess(emptyResponse);

        Mockito.verify(MOCK_JMS_TEMPLATE, Mockito.never()).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
    }

    @Test
    public void postProcessWithResultsTest() {
        ProviderCallbackIssueTrackerResponsePostProcessor postProcessor = new ProviderCallbackIssueTrackerResponsePostProcessor(EVENT_MANAGER);

        List<IssueTrackerIssueResponseModel<String>> responseModels = List.of(ISSUE_RESPONSE_MODEL);
        IssueTrackerResponse<String> populatedResponse = new IssueTrackerResponse<>("Example Status Message", responseModels);
        postProcessor.postProcess(populatedResponse);

        Mockito.verify(MOCK_JMS_TEMPLATE, Mockito.times(1)).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
    }

}
