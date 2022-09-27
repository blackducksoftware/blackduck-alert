package com.synopsys.integration.alert.api.channel.issue.send;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerModelHolder;
import com.synopsys.integration.alert.api.distribution.audit.AuditSuccessEvent;
import com.synopsys.integration.alert.api.event.AlertEvent;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.common.persistence.accessor.JobSubTaskAccessor;

public class IssueTrackerAsyncMessageSender<T extends Serializable> {

    private final IssueTrackerCreationEventGenerator issueCreateEventGenerator;
    private final IssueTrackerTransitionEventGenerator<T> issueTrackerTransitionEventGenerator;
    private final IssueTrackerCommentEventGenerator<T> issueTrackerCommentEventGenerator;
    private final EventManager eventManager;
    private final JobSubTaskAccessor jobSubTaskAccessor;
    private final UUID parentEventId;
    private final UUID jobId;
    private final Set<Long> notificationIds;

    public IssueTrackerAsyncMessageSender(
        IssueTrackerCreationEventGenerator issueCreateEventGenerator,
        IssueTrackerTransitionEventGenerator<T> issueTrackerTransitionEventGenerator,
        IssueTrackerCommentEventGenerator<T> issueTrackerCommentEventGenerator,
        EventManager eventManager,
        JobSubTaskAccessor jobSubTaskAccessor,
        UUID parentEventId,
        UUID jobId,
        Set<Long> notificationIds
    ) {
        this.issueCreateEventGenerator = issueCreateEventGenerator;
        this.issueTrackerTransitionEventGenerator = issueTrackerTransitionEventGenerator;
        this.issueTrackerCommentEventGenerator = issueTrackerCommentEventGenerator;
        this.eventManager = eventManager;
        this.jobSubTaskAccessor = jobSubTaskAccessor;
        this.parentEventId = parentEventId;
        this.jobId = jobId;
        this.notificationIds = notificationIds;
    }

    public final void sendAsyncMessages(List<IssueTrackerModelHolder<T>> issueTrackerMessages) {
        List<AlertEvent> eventList = issueTrackerMessages.stream()
            .map(this::createAlertEvents)
            .flatMap(List::stream)
            .collect(Collectors.toList());

        if (eventList.isEmpty()) {
            // nothing further to send downstream. Channel handled message successfully.
            eventManager.sendEvent(new AuditSuccessEvent(jobId, notificationIds));
        } else {
            jobSubTaskAccessor.updateTaskCount(parentEventId, (long) eventList.size());
            eventManager.sendEvents(eventList);
        }
    }

    @NotNull
    private List<AlertEvent> createAlertEvents(IssueTrackerModelHolder<T> issueTrackerMessage) {
        List<AlertEvent> eventList = new LinkedList<>();
        eventList.addAll(createMessages(issueTrackerMessage.getIssueCreationModels(), issueCreateEventGenerator::generateEvent));
        eventList.addAll(createMessages(issueTrackerMessage.getIssueTransitionModels(), issueTrackerTransitionEventGenerator::generateEvent));
        eventList.addAll(createMessages(issueTrackerMessage.getIssueCommentModels(), issueTrackerCommentEventGenerator::generateEvent));
        return eventList;
    }

    private <U> List<AlertEvent> createMessages(List<U> messages, Function<U, AlertEvent> eventGenerator) {
        return messages.stream()
            .map(eventGenerator::apply)
            .collect(Collectors.toList());
    }

}
