package com.synopsys.integration.alert.api.channel.issue.callback;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerResponse;
import com.synopsys.integration.alert.api.event.AlertEventHandler;
import com.synopsys.integration.alert.common.channel.issuetracker.IssueTrackerCallbackEvent;
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

    private static TestIssueTrackHandler EVENT_HANDLER;

    @BeforeAll
    public static void init() {
        EVENT_HANDLER = new TestIssueTrackHandler();
    }

    @Test
    public void postProcessNoResultsTest() {
        ProviderCallbackIssueTrackerResponsePostProcessor postProcessor = new ProviderCallbackIssueTrackerResponsePostProcessor(EVENT_HANDLER);

        IssueTrackerResponse<String> emptyResponse = new IssueTrackerResponse<>("Example Status Message", List.of());
        postProcessor.postProcess(emptyResponse);

        assertFalse(EVENT_HANDLER.isHandleEventCalled());
    }

    @Test
    public void postProcessWithResultsTest() {
        ProviderCallbackIssueTrackerResponsePostProcessor postProcessor = new ProviderCallbackIssueTrackerResponsePostProcessor(EVENT_HANDLER);

        List<IssueTrackerIssueResponseModel<String>> responseModels = List.of(ISSUE_RESPONSE_MODEL);
        IssueTrackerResponse<String> populatedResponse = new IssueTrackerResponse<>("Example Status Message", responseModels);
        postProcessor.postProcess(populatedResponse);

        assertTrue(EVENT_HANDLER.isHandleEventCalled());
    }

    private static class TestIssueTrackHandler implements AlertEventHandler<IssueTrackerCallbackEvent> {
        private boolean handleEventCalled = false;

        @Override
        public void handle(IssueTrackerCallbackEvent event) {
            this.handleEventCalled = true;
        }

        public boolean isHandleEventCalled() {
            return handleEventCalled;
        }
    }

}
