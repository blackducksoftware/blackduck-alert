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
import java.util.Optional;

import javax.annotation.Nullable;

import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.blackduck.integration.jira.common.cloud.model.AtlassianDocumentFormatModel;

public class IssueTransitionModel<T extends Serializable> extends AlertSerializableModel {
    private final ExistingIssueDetails<T> existingIssueDetails;
    private final IssueOperation issueOperation;
    private final List<String> postTransitionComments;
    private final List<AtlassianDocumentFormatModel> atlassianTransitionComments;

    private final ProjectIssueModel source;

    public IssueTransitionModel(
        ExistingIssueDetails<T> existingIssueDetails,
        IssueOperation issueOperation,
        List<String> postTransitionComments,
        ProjectIssueModel source
    ) {
        this(existingIssueDetails, issueOperation, postTransitionComments, source, null);
    }

    public IssueTransitionModel(
        ExistingIssueDetails<T> existingIssueDetails,
        IssueOperation issueOperation,
        List<String> postTransitionComments,
        ProjectIssueModel source,
        @Nullable List<AtlassianDocumentFormatModel> atlassianTransitionComments
    ) {
        this.existingIssueDetails = existingIssueDetails;
        this.issueOperation = issueOperation;
        this.postTransitionComments = postTransitionComments;
        this.source = source;
        this.atlassianTransitionComments = atlassianTransitionComments;
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

    public Optional<List<AtlassianDocumentFormatModel>> getAtlassianTransitionComments() {
        return Optional.ofNullable(atlassianTransitionComments);
    }

}
