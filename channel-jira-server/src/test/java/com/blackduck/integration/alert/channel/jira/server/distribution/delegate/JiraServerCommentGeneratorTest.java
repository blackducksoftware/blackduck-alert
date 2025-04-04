/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.distribution.delegate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerCommentEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCommentModel;
import com.blackduck.integration.alert.api.descriptor.JiraServerChannelKey;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.channel.jira.server.distribution.event.JiraServerCommentEvent;

class JiraServerCommentGeneratorTest {
	@Test
	void generateEventAttributeMatches() {
		JiraServerChannelKey testKey = ChannelKeys.JIRA_SERVER;
		UUID testJobExecutionID = UUID.randomUUID();
		UUID testJobUID = UUID.randomUUID();
		Set<Long> testNotificationIds = Set.of(1L, 2L, 3L, 5L);
		IssueCommentModel<String> testModel = Mockito.mock(IssueCommentModel.class);

		JiraServerCommentGenerator testGenerator = new JiraServerCommentGenerator(testKey, testJobExecutionID, testJobUID, testNotificationIds);
		IssueTrackerCommentEvent<String> generatedCommentEvent = testGenerator.generateEvent(testModel);

		assertEquals(generatedCommentEvent.getClass(), JiraServerCommentEvent.class);
		assertAll(
			"Constructed IssueTrackerCommentEvent matches generator attributes",
			() -> assertEquals(IssueTrackerCommentEvent.createDefaultEventDestination(testKey), generatedCommentEvent.getDestination()),
			() -> assertEquals(testJobExecutionID, generatedCommentEvent.getJobExecutionId()),
			() -> assertEquals(testJobUID, generatedCommentEvent.getJobId()),
			() -> assertEquals(testNotificationIds, generatedCommentEvent.getNotificationIds()),
			() -> assertEquals(testModel, generatedCommentEvent.getCommentModel())
		);
	}
}
