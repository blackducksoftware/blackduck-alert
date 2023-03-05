package com.synopsys.integration.alert.api.channel.issue.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.channel.issue.model.IssueCommentModel;
import com.synopsys.integration.alert.api.descriptor.JiraServerChannelKey;

class IssueTrackerCommentEventTest {
    @Test
    void testObjectConstruction() {
        String destination = "destination_queue";
        UUID parentEventId = UUID.randomUUID();
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L, 4L);
        IssueCommentModel<String> model = new IssueCommentModel<>(null, List.of(), null);
        IssueTrackerCommentEvent<String> event = new IssueTrackerCommentEvent<>(destination, parentEventId, jobExecutionId, jobId, notificationIds, model);

        assertNotNull(event.getEventId());
        assertEquals(parentEventId, event.getParentEventId());
        assertEquals(jobExecutionId, event.getJobExecutionId());
        assertEquals(destination, event.getDestination());
        assertEquals(jobId, event.getJobId());
        assertEquals(notificationIds, event.getNotificationIds());
        assertEquals(model, event.getCommentModel());
    }

    @Test
    void createDefaultEventDestinationTest() {
        JiraServerChannelKey channelKey = new JiraServerChannelKey();
        String defaultEventDestination = IssueTrackerCommentEvent.createDefaultEventDestination(channelKey);

        assertEquals("channel_jira_server_issue_comment", defaultEventDestination);
    }

    @Test
    void getCommentModelTest() {
        IssueCommentModel<String> model = new IssueCommentModel<>(null, List.of(), null);
        IssueTrackerCommentEvent<String> event = new IssueTrackerCommentEvent<>(null, null, null, null, null, model);

        IssueCommentModel<String> testModel = event.getCommentModel();
        assertEquals(model, testModel);
    }
}
