package com.synopsys.integration.alert.channel.jira.server.distribution.delegate;

import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerCreateIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.channel.jira.server.distribution.event.JiraServerCreateIssueEvent;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JiraServerCreateEventGeneratorTest {
    @Test
    void generateEventAttributeMatches() {
        JiraServerChannelKey testKey = ChannelKeys.JIRA_SERVER;
        UUID testParentEventUID = UUID.randomUUID();
        UUID testJobUID = UUID.randomUUID();
        Set<Long> testNotificationIds = Set.of(1L, 2L, 3L, 5L);
        IssueCreationModel testModel = Mockito.mock(IssueCreationModel.class);

        JiraServerCreateEventGenerator testGenerator = new JiraServerCreateEventGenerator(testKey, testParentEventUID, testJobUID, testNotificationIds);
        IssueTrackerCreateIssueEvent generatedCreateEvent = testGenerator.generateEvent(testModel);

        assertEquals(generatedCreateEvent.getClass(), JiraServerCreateIssueEvent.class);
        assertAll("Constructed IssueTrackerCreateIssueEvent matches generator attributes",
            () -> assertEquals(IssueTrackerCreateIssueEvent.createDefaultEventDestination(testKey), generatedCreateEvent.getDestination()),
            () -> assertEquals(testParentEventUID, generatedCreateEvent.getParentEventId()),
            () -> assertEquals(testJobUID, generatedCreateEvent.getJobId()),
            () -> assertEquals(testNotificationIds, generatedCreateEvent.getNotificationIds()),
            () -> assertEquals(testModel, generatedCreateEvent.getCreationModel())
        );
    }
}