/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue.model;

import java.io.Serializable;
import java.util.List;

import com.synopsys.integration.alert.channel.api.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class IssueTransitionModel<T extends Serializable> extends AlertSerializableModel {
    private final ExistingIssueDetails<T> existingIssueDetails;
    private final IssueOperation issueOperation;
    private final List<String> postTransitionComments;

    private final ProjectIssueModel source;

    public IssueTransitionModel(
        ExistingIssueDetails<T> existingIssueDetails,
        IssueOperation issueOperation,
        List<String> postTransitionComments,
        ProjectIssueModel source
    ) {
        this.existingIssueDetails = existingIssueDetails;
        this.issueOperation = issueOperation;
        this.postTransitionComments = postTransitionComments;
        this.source = source;
    }

    public ExistingIssueDetails<T> getExistingIssueDetails() {
        return existingIssueDetails;
    }

    public IssueOperation getIssueOperation() {
        return issueOperation;
    }

    public List<String> getPostTransitionComments() {
        return postTransitionComments;
    }

    public ProjectIssueModel getSource() {
        return source;
    }

}
