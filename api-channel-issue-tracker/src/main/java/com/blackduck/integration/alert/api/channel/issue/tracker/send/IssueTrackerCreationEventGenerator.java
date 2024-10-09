package com.blackduck.integration.alert.api.channel.issue.tracker.send;

import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerCreateIssueEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;

public interface IssueTrackerCreationEventGenerator {
    IssueTrackerCreateIssueEvent generateEvent(IssueCreationModel model);
}
