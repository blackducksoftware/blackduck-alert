/*
 * api-channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue.model;

import java.io.Serializable;
import java.util.Collection;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

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
