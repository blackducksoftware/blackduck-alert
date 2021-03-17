/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue.model;

import java.util.Collection;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class IssueTrackerResponse extends AlertSerializableModel {
    private final String statusMessage;
    private final Collection<IssueTrackerIssueResponseModel> updatedIssues;

    public IssueTrackerResponse(String statusMessage, Collection<IssueTrackerIssueResponseModel> updatedIssues) {
        this.statusMessage = statusMessage;
        this.updatedIssues = updatedIssues;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public Collection<IssueTrackerIssueResponseModel> getUpdatedIssues() {
        return updatedIssues;
    }

}
