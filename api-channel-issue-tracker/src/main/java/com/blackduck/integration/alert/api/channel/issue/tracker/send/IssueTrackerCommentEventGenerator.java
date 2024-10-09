package com.blackduck.integration.alert.api.channel.issue.tracker.send;

import java.io.Serializable;

import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerCommentEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCommentModel;

public interface IssueTrackerCommentEventGenerator<T extends Serializable> {

    IssueTrackerCommentEvent<T> generateEvent(IssueCommentModel<T> model);

}
