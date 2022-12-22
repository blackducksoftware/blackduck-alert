package com.synopsys.integration.alert.channel.jira.server.distribution.delegate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerCommentEvent;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCommentModel;
import com.synopsys.integration.alert.channel.jira.server.distribution.event.JiraServerCommentEvent;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

public class JiraServerCommentGeneratorTest {
	@Test
	void generateEventAttributeMatches() {
		JiraServerChannelKey testKey = ChannelKeys.JIRA_SERVER;
		UUID testParentEventUID = UUID.randomUUID();
		UUID testJobExecutionID = UUID.randomUUID();
		UUID testJobUID = UUID.randomUUID();
		Set<Long> testNotificationIds = Set.of(1L, 2L, 3L , 5L);
		IssueCommentModel<String> testModel = Mockito.mock(IssueCommentModel.class);

		JiraServerCommentGenerator testGenerator = new JiraServerCommentGenerator(testKey, testParentEventUID, testJobExecutionID, testJobUID, testNotificationIds);
		IssueTrackerCommentEvent<String> generatedCommentEvent = testGenerator.generateEvent(testModel);

		assertEquals(generatedCommentEvent.getClass(), JiraServerCommentEvent.class);
		assertAll(
			"Constructed IssueTrackerCommentEvent matches generator attributes",
			() -> assertEquals(IssueTrackerCommentEvent.createDefaultEventDestination(testKey), generatedCommentEvent.getDestination()),
			() -> assertEquals(testParentEventUID, generatedCommentEvent.getParentEventId()),
			() -> assertEquals(testJobExecutionID, generatedCommentEvent.getJobExecutionId()),
			() -> assertEquals(testJobUID, generatedCommentEvent.getJobId()),
			() -> assertEquals(testNotificationIds, generatedCommentEvent.getNotificationIds()),
			() -> assertEquals(testModel, generatedCommentEvent.getCommentModel())
		);
	}
}
