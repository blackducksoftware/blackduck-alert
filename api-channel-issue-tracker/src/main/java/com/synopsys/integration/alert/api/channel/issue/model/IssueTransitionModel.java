/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.model;

import java.io.Serializable;
import java.util.List;

import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;

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
