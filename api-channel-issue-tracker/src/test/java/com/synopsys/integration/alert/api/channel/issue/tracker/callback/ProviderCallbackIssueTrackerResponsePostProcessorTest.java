package com.synopsys.integration.alert.api.channel.issue.tracker.callback;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.task.SyncTaskExecutor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueTrackerResponse;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.blackduck.integration.alert.common.channel.issuetracker.message.IssueTrackerCallbackInfo;

class ProviderCallbackIssueTrackerResponsePostProcessorTest {
    private static final Gson GSON = new GsonBuilder().create();
    private static final IssueTrackerIssueResponseModel<String> ISSUE_RESPONSE_MODEL = new IssueTrackerIssueResponseModel<>(
        "issue-id",
        "issue-key",
        "https://issue-link",
        "Issue Title",
        IssueOperation.OPEN,
        new IssueTrackerCallbackInfo(0L, "https://callback-info", "https://project-version-url")
    );

    private static RabbitTemplate MOCK_RABBIT_TEMPLATE;
    private static EventManager EVENT_MANAGER;
    private static AtomicInteger SEND_COUNTER = new AtomicInteger(0);

    @BeforeAll
    public static void init() {
        MOCK_RABBIT_TEMPLATE = Mockito.mock(RabbitTemplate.class);
        Mockito.doAnswer((answer -> {
            SEND_COUNTER.incrementAndGet();
            return null;
        })).when(MOCK_RABBIT_TEMPLATE).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));

        EVENT_MANAGER = new EventManager(GSON, MOCK_RABBIT_TEMPLATE, new SyncTaskExecutor());
    }

    @Test
    void postProcessNoResultsTest() {
        ProviderCallbackIssueTrackerResponsePostProcessor postProcessor = new ProviderCallbackIssueTrackerResponsePostProcessor(EVENT_MANAGER);

        IssueTrackerResponse<String> emptyResponse = new IssueTrackerResponse<>("Example Status Message", List.of());
        postProcessor.postProcess(emptyResponse);

        assertEquals(0, SEND_COUNTER.get());
    }

    @Test
    void postProcessWithResultsTest() {
        ProviderCallbackIssueTrackerResponsePostProcessor postProcessor = new ProviderCallbackIssueTrackerResponsePostProcessor(EVENT_MANAGER);

        List<IssueTrackerIssueResponseModel<String>> responseModels = List.of(ISSUE_RESPONSE_MODEL);
        IssueTrackerResponse<String> populatedResponse = new IssueTrackerResponse<>("Example Status Message", responseModels);
        postProcessor.postProcess(populatedResponse);

        assertEquals(1, SEND_COUNTER.get());
    }
}
