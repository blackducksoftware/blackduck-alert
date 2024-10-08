package com.blackduck.integration.alert.channel.jira.cloud.distribution.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.channel.jira.cloud.distribution.event.JiraCloudCommentEvent;
import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueCommentModel;

class JiraCloudCommentIssueEventTest {
    @Test
    void testObjectConstruction() {
        String destination = "destination_queue";
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L, 4L);
        IssueCommentModel<String> model = new IssueCommentModel<>(null, List.of(), null);
        JiraCloudCommentEvent event = new JiraCloudCommentEvent(destination, jobExecutionId, jobId, notificationIds, model);

        assertNotNull(event.getEventId());
        assertEquals(jobExecutionId, event.getJobExecutionId());
        assertEquals(destination, event.getDestination());
        assertEquals(jobId, event.getJobId());
        assertEquals(notificationIds, event.getNotificationIds());
        assertEquals(model, event.getCommentModel());
    }
}
