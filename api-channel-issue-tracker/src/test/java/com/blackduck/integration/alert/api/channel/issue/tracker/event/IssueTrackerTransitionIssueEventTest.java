/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTransitionModel;
import com.blackduck.integration.alert.api.descriptor.JiraServerChannelKey;
import com.blackduck.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;

class IssueTrackerTransitionIssueEventTest {

    @Test
    void testObjectConstruction() {
        String destination = "destination_queue";
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L, 4L);
        IssueTransitionModel<String> model = new IssueTransitionModel<>(null, IssueOperation.UPDATE, List.of(), null);
        IssueTrackerTransitionIssueEvent<String> event = new IssueTrackerTransitionIssueEvent<>(destination, jobExecutionId, jobId, notificationIds, model);

        assertNotNull(event.getEventId());
        assertEquals(jobExecutionId, event.getJobExecutionId());
        assertEquals(destination, event.getDestination());
        assertEquals(jobId, event.getJobId());
        assertEquals(notificationIds, event.getNotificationIds());
        assertEquals(model, event.getTransitionModel());
    }

    @Test
    void createDefaultEventDestinationTest() {
        JiraServerChannelKey channelKey = new JiraServerChannelKey();
        String defaultEventDestination = IssueTrackerTransitionIssueEvent.createDefaultEventDestination(channelKey);

        assertEquals("channel_jira_server_issue_transition", defaultEventDestination);
    }

    @Test
    void getTransitionModelTest() {
        IssueTransitionModel<String> model = new IssueTransitionModel<>(null, IssueOperation.UPDATE, List.of(), null);
        IssueTrackerTransitionIssueEvent<String> event = new IssueTrackerTransitionIssueEvent<>(null, null, null, null, model);

        IssueTransitionModel<String> testModel = event.getTransitionModel();
        assertEquals(model, testModel);
    }
}
