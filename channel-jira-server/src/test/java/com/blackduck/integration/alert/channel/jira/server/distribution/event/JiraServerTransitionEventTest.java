package com.blackduck.integration.alert.channel.jira.server.distribution.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueTransitionModel;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;

class JiraServerTransitionEventTest {
    @Test
    void testObjectConstruction() {
        String destination = "destination_queue";
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L, 4L);
        IssueTransitionModel<String> model = new IssueTransitionModel<>(null, IssueOperation.UPDATE, List.of(), null);
        JiraServerTransitionEvent event = new JiraServerTransitionEvent(destination, jobExecutionId, jobId, notificationIds, model);

        assertNotNull(event.getEventId());
        assertEquals(jobExecutionId, event.getJobExecutionId());
        assertEquals(destination, event.getDestination());
        assertEquals(jobId, event.getJobId());
        assertEquals(notificationIds, event.getNotificationIds());
        assertEquals(model, event.getTransitionModel());
    }
}
