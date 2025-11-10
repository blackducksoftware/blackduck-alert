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

import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.blackduck.integration.jira.common.cloud.model.AtlassianDocumentFormatModel;
import org.jetbrains.annotations.Nullable;

public class IssueTransitionModel<T extends Serializable> extends AlertSerializableModel {
    private final ExistingIssueDetails<T> existingIssueDetails;
    private final IssueOperation issueOperation;
    private final List<String> postTransitionComments;

    private final ProjectIssueModel source;
    @Nullable
    private final AtlassianDocumentFormatModel atlassianDocumentFormatCommentModel;

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
        @Nullable AtlassianDocumentFormatModel atlassianDocumentFormatCommentModel
    ) {
        this.existingIssueDetails = existingIssueDetails;
        this.issueOperation = issueOperation;
        this.postTransitionComments = postTransitionComments;
        this.source = source;
        this.atlassianDocumentFormatCommentModel = atlassianDocumentFormatCommentModel;
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

    public Optional<AtlassianDocumentFormatModel> getAtlassianDocumentFormatCommentModel() {
        return Optional.ofNullable(atlassianDocumentFormatCommentModel);
    }

}
