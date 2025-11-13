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

import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.jira.common.cloud.model.AtlassianDocumentFormatModel;

public class IssueCommentModel<T extends Serializable> extends AlertSerializableModel {
    private final ExistingIssueDetails<T> existingIssueDetails;
    private final List<String> comments;
    @Nullable
    private final ProjectIssueModel source;
    @Nullable
    private final AtlassianDocumentFormatModel atlassianDocumentFormatCommentModel;
    @Nullable
    private final List<AtlassianDocumentFormatModel> additionalComments;

    public IssueCommentModel(ExistingIssueDetails<T> existingIssueDetails, List<String> comments, @Nullable ProjectIssueModel source) {
        this(existingIssueDetails, comments, source, null, null);
    }

    public IssueCommentModel(
        ExistingIssueDetails<T> existingIssueDetails,
        List<String> comments,
        @Nullable ProjectIssueModel source,
        @Nullable AtlassianDocumentFormatModel atlassianDocumentFormatCommentModel,
        @Nullable List<AtlassianDocumentFormatModel> additionalComments
    ) {
        this.existingIssueDetails = existingIssueDetails;
        this.comments = comments;
        this.source = source;
        this.atlassianDocumentFormatCommentModel = atlassianDocumentFormatCommentModel;
        this.additionalComments = additionalComments;
    }

    public ExistingIssueDetails<T> getExistingIssueDetails() {
        return existingIssueDetails;
    }

    public List<String> getComments() {
        return comments;
    }

    public Optional<ProjectIssueModel> getSource() {
        return Optional.ofNullable(source);
    }

    public Optional<AtlassianDocumentFormatModel> getAtlassianDocumentFormatCommentModel() {
        return Optional.ofNullable(atlassianDocumentFormatCommentModel);
    }

    public Optional<List<AtlassianDocumentFormatModel>> getAdditionalComments() {
        return Optional.ofNullable(additionalComments);
    }

}
