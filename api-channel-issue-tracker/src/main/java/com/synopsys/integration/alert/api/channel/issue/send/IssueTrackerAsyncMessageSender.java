package com.synopsys.integration.alert.api.channel.issue.send;

import java.io.Serializable;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerModelHolder;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.distribution.execution.JobStage;
import com.synopsys.integration.alert.api.distribution.execution.JobStageStartedEvent;
import com.synopsys.integration.alert.api.event.AlertEvent;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.accessor.JobSubTaskAccessor;

public class IssueTrackerAsyncMessageSender<T extends Serializable> {
    private final IssueTrackerCreationEventGenerator issueCreateEventGenerator;
    private final IssueTrackerTransitionEventGenerator<T> issueTrackerTransitionEventGenerator;
    private final IssueTrackerCommentEventGenerator<T> issueTrackerCommentEventGenerator;
    private final EventManager eventManager;
    private final JobSubTaskAccessor jobSubTaskAccessor;
    private final UUID parentEventId;
    private final UUID jobExecutionId;
    private final Set<Long> notificationIds;
    private final ExecutingJobManager executingJobManager;

    public IssueTrackerAsyncMessageSender(
        IssueTrackerCreationEventGenerator issueCreateEventGenerator,
        IssueTrackerTransitionEventGenerator<T> issueTrackerTransitionEventGenerator,
        IssueTrackerCommentEventGenerator<T> issueTrackerCommentEventGenerator,
        EventManager eventManager,
        JobSubTaskAccessor jobSubTaskAccessor,
        UUID parentEventId,
        UUID jobExecutionId,
        Set<Long> notificationIds,
        ExecutingJobManager executingJobManager
    ) {
        this.issueCreateEventGenerator = issueCreateEventGenerator;
        this.issueTrackerTransitionEventGenerator = issueTrackerTransitionEventGenerator;
        this.issueTrackerCommentEventGenerator = issueTrackerCommentEventGenerator;
        this.eventManager = eventManager;
        this.jobSubTaskAccessor = jobSubTaskAccessor;
        this.parentEventId = parentEventId;
        this.jobExecutionId = jobExecutionId;
        this.notificationIds = notificationIds;
        this.executingJobManager = executingJobManager;
    }

    public final void sendAsyncMessages(List<IssueTrackerModelHolder<T>> issueTrackerMessages) {
        List<AlertEvent> eventList = issueTrackerMessages.stream()
            .map(this::createAlertEvents)
            .flatMap(List::stream)
            .collect(Collectors.toList());

        // the full set of notifications to be sent is here.  Each event generated is for a subset of notification ids.
        // some notifications do not produce events which is why the check for the empty event list also exists.
        executingJobManager.incrementSentNotificationCount(jobExecutionId, notificationIds.size());
        if (eventList.isEmpty()) {
            jobSubTaskAccessor.removeSubTaskStatus(parentEventId);
            if (!executingJobManager.hasRemainingEvents(jobExecutionId)
                && executingJobManager.hasSentExpectedNotifications(jobExecutionId)) {
                executingJobManager.updateJobStatus(jobExecutionId, AuditEntryStatus.SUCCESS);
                executingJobManager.endJob(jobExecutionId, Instant.now());
            }
        } else {
            jobSubTaskAccessor.updateTaskCount(parentEventId, (long) eventList.size());
            executingJobManager.incrementRemainingEvents(jobExecutionId, eventList.size());
            eventManager.sendEvents(eventList);
        }
    }

    @NotNull
    private List<AlertEvent> createAlertEvents(IssueTrackerModelHolder<T> issueTrackerMessage) {
        List<AlertEvent> eventList = new LinkedList<>();
        List<AlertEvent> creationEvents = createMessages(issueTrackerMessage.getIssueCreationModels(), issueCreateEventGenerator::generateEvent);
        List<AlertEvent> transitionEvents = createMessages(issueTrackerMessage.getIssueTransitionModels(), issueTrackerTransitionEventGenerator::generateEvent);
        List<AlertEvent> commentEvents = createMessages(issueTrackerMessage.getIssueCommentModels(), issueTrackerCommentEventGenerator::generateEvent);

        addEventsAndStartStage(eventList, creationEvents, JobStage.ISSUE_CREATION);
        addEventsAndStartStage(eventList, transitionEvents, JobStage.ISSUE_TRANSITION);
        addEventsAndStartStage(eventList, commentEvents, JobStage.ISSUE_COMMENTING);

        return eventList;
    }

    private <U> List<AlertEvent> createMessages(List<U> messages, Function<U, AlertEvent> eventGenerator) {
        return messages.stream()
            .map(eventGenerator::apply)
            .collect(Collectors.toList());
    }

    private void addEventsAndStartStage(List<AlertEvent> allEvents, List<AlertEvent> events, JobStage jobStage) {
        if (!events.isEmpty()) {
            eventManager.sendEvent(new JobStageStartedEvent(jobExecutionId, jobStage, Instant.now().toEpochMilli()));
            allEvents.addAll(events);
        }
    }

}
