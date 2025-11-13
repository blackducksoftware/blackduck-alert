package com.blackduck.integration.alert.api.channel.issue.tracker.model;

import java.util.List;

import com.blackduck.integration.alert.api.event.AlertEvent;

public class IssueTrackerEventModel {
    private final List<AlertEvent> creationEvents;
    private final List<AlertEvent> commentEvents;
    private final List<AlertEvent> transitionEvents;

    public IssueTrackerEventModel(List<AlertEvent> creationEvents, List<AlertEvent> commentEvents, List<AlertEvent> transitionEvents) {
        this.creationEvents = creationEvents;
        this.commentEvents = commentEvents;
        this.transitionEvents = transitionEvents;
    }

    public List<AlertEvent> getIssueCreationEvents() {
        if (creationEvents == null) {
            return List.of();
        }
        return creationEvents;
    }

    public List<AlertEvent> getIssueCommentEvents() {
        if (commentEvents == null) {
            return List.of();
        }
        return commentEvents;
    }

    public List<AlertEvent> getIssueTransitionEvents() {
        if (transitionEvents == null) {
            return List.of();
        }
        return transitionEvents;
    }
}
