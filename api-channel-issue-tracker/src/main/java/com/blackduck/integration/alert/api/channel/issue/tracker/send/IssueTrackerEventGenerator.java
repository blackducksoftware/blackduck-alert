package com.blackduck.integration.alert.api.channel.issue.tracker.send;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerEventModel;

public interface IssueTrackerEventGenerator<M> {
    IssueTrackerEventModel generateEvents(M model);

}
