package com.blackduck.integration.alert.channel.jira.server.distribution.delegate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.channel.jira.server.distribution.event.JiraServerCreateIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.tracker.event.IssueTrackerCreateIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.descriptor.JiraServerChannelKey;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;

class JiraServerCreateEventGeneratorTest {
    @Test
    void generateEventAttributeMatches() {
        JiraServerChannelKey testKey = ChannelKeys.JIRA_SERVER;
        UUID testJobExecutionID = UUID.randomUUID();
        UUID testJobUID = UUID.randomUUID();
        Set<Long> testNotificationIds = Set.of(1L, 2L, 3L, 5L);
        IssueCreationModel testModel = Mockito.mock(IssueCreationModel.class);

        JiraServerCreateEventGenerator testGenerator = new JiraServerCreateEventGenerator(testKey, testJobExecutionID, testJobUID, testNotificationIds);
        IssueTrackerCreateIssueEvent generatedCreateEvent = testGenerator.generateEvent(testModel);

        assertEquals(generatedCreateEvent.getClass(), JiraServerCreateIssueEvent.class);
        assertAll(
            "Constructed IssueTrackerCreateIssueEvent matches generator attributes",
            () -> assertEquals(IssueTrackerCreateIssueEvent.createDefaultEventDestination(testKey), generatedCreateEvent.getDestination()),
            () -> assertEquals(testJobExecutionID, generatedCreateEvent.getJobExecutionId()),
            () -> assertEquals(testJobUID, generatedCreateEvent.getJobId()),
            () -> assertEquals(testNotificationIds, generatedCreateEvent.getNotificationIds()),
            () -> assertEquals(testModel, generatedCreateEvent.getCreationModel())
        );
    }
}