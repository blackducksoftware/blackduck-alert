package com.blackduck.integration.alert.api.channel.issue.tracker.model;

import java.io.Serializable;
import java.util.Collection;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class IssueTrackerResponse<T extends Serializable> extends AlertSerializableModel {
    private final String statusMessage;
    private final Collection<IssueTrackerIssueResponseModel<T>> updatedIssues;

    public IssueTrackerResponse(String statusMessage, Collection<IssueTrackerIssueResponseModel<T>> updatedIssues) {
        this.statusMessage = statusMessage;
        this.updatedIssues = updatedIssues;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public Collection<IssueTrackerIssueResponseModel<T>> getUpdatedIssues() {
        return updatedIssues;
    }

}
