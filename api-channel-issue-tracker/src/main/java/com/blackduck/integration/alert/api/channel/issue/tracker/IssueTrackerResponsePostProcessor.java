package com.blackduck.integration.alert.api.channel.issue.tracker;

import java.io.Serializable;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerResponse;

public interface IssueTrackerResponsePostProcessor {
    <T extends Serializable> void postProcess(IssueTrackerResponse<T> response);

}
