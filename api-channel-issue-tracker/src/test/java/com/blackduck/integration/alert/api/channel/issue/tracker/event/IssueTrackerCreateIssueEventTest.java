package com.blackduck.integration.alert.api.channel.issue.tracker.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.descriptor.JiraServerChannelKey;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

class IssueTrackerCreateIssueEventTest {

    @Test
    void testObjectConstruction() {
        String destination = "destination_queue";
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L, 4L);
        IssueCreationModel model = IssueCreationModel.simple("title", "description", List.of(), new LinkableItem("providerLabel", "provider"));
        IssueTrackerCreateIssueEvent event = new IssueTrackerCreateIssueEvent(destination, jobExecutionId, jobId, notificationIds, model);

        assertNotNull(event.getEventId());
        assertEquals(jobExecutionId, event.getJobExecutionId());
        assertEquals(destination, event.getDestination());
        assertEquals(jobId, event.getJobId());
        assertEquals(notificationIds, event.getNotificationIds());
    }

    @Test
    void createDefaultEventDestinationTest() {
        JiraServerChannelKey channelKey = new JiraServerChannelKey();
        String defaultEventDestination = IssueTrackerCreateIssueEvent.createDefaultEventDestination(channelKey);

        assertEquals("channel_jira_server_issue_create", defaultEventDestination);
    }

    @Test
    void getCreationModelTest() {
        IssueCreationModel model = IssueCreationModel.simple("title", "description", List.of(), new LinkableItem("providerLabel", "provider"));
        IssueTrackerCreateIssueEvent event = new IssueTrackerCreateIssueEvent(null, null, null, null, model);

        IssueCreationModel testModel = event.getCreationModel();
        assertEquals(model, testModel);
    }
}
