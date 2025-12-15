/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.send;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerEventModel;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.distribution.execution.JobStage;
import com.blackduck.integration.alert.api.distribution.execution.JobStageStartedEvent;
import com.blackduck.integration.alert.api.event.AlertEvent;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.common.enumeration.AuditEntryStatus;

public class IssueTrackerAsyncMessageSender<T> implements AsyncMessageSender<T> {
    private final IssueTrackerEventGenerator<T> issueTrackerEventGenerator;
    private final EventManager eventManager;
    private final UUID jobExecutionId;
    private final Set<Long> notificationIds;
    private final ExecutingJobManager executingJobManager;

    public IssueTrackerAsyncMessageSender(
        IssueTrackerEventGenerator<T> issueTrackerEventGenerator,
        EventManager eventManager,
        UUID jobExecutionId,
        Set<Long> notificationIds,
        ExecutingJobManager executingJobManager
    ) {
        this.issueTrackerEventGenerator = issueTrackerEventGenerator;
        this.eventManager = eventManager;
        this.jobExecutionId = jobExecutionId;
        this.notificationIds = notificationIds;
        this.executingJobManager = executingJobManager;
    }

    public final void sendAsyncMessages(List<T> issueTrackerMessages) {
        List<AlertEvent> eventList = issueTrackerMessages.stream()
            .map(this::createAlertEvents)
            .flatMap(List::stream)
            .toList();

        // the full set of notifications to be sent is here.  Each event generated is for a subset of notification ids.
        // some notifications do not produce events which is why the check for the empty event list also exists.
        executingJobManager.incrementSentNotificationCount(jobExecutionId, notificationIds.size());
        if (eventList.isEmpty()) {
            if (!executingJobManager.hasRemainingEvents(jobExecutionId)
                && executingJobManager.hasSentExpectedNotifications(jobExecutionId)) {
                executingJobManager.updateJobStatus(jobExecutionId, AuditEntryStatus.SUCCESS);
                executingJobManager.endJob(jobExecutionId, Instant.now());
            }
        } else {
            executingJobManager.incrementRemainingEvents(jobExecutionId, eventList.size());
            eventManager.sendEvents(eventList);
        }
    }

    @NotNull
    private List<AlertEvent> createAlertEvents(T issueTrackerMessage) {
        List<AlertEvent> eventList = new LinkedList<>();
        IssueTrackerEventModel eventModel = issueTrackerEventGenerator.generateEvents(issueTrackerMessage);
        List<AlertEvent> creationEvents = eventModel.getIssueCreationEvents();
        List<AlertEvent> transitionEvents = eventModel.getIssueTransitionEvents();
        List<AlertEvent> commentEvents = eventModel.getIssueCommentEvents();

        addEventsAndStartStage(eventList, creationEvents, JobStage.ISSUE_CREATION);
        addEventsAndStartStage(eventList, transitionEvents, JobStage.ISSUE_TRANSITION);
        addEventsAndStartStage(eventList, commentEvents, JobStage.ISSUE_COMMENTING);

        return eventList;
    }

    private void addEventsAndStartStage(List<AlertEvent> allEvents, List<AlertEvent> events, JobStage jobStage) {
        if (!events.isEmpty()) {
            eventManager.sendEvent(new JobStageStartedEvent(jobExecutionId, jobStage, Instant.now().toEpochMilli()));
            allEvents.addAll(events);
        }
    }

}
