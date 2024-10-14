/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.model;

import java.io.Serializable;
import java.util.List;

import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;

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
