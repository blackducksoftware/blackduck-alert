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

import com.blackduck.integration.jira.common.cloud.model.AtlassianDocumentFormatModel;
import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class IssueCommentModel<T extends Serializable> extends AlertSerializableModel {
    private final ExistingIssueDetails<T> existingIssueDetails;
    private final List<String> comments;
    @Nullable
    private final ProjectIssueModel source;
    @Nullable
    private final AtlassianDocumentFormatModel atlassianDocumentFormatCommentModel;

    public IssueCommentModel(ExistingIssueDetails<T> existingIssueDetails, List<String> comments, @Nullable ProjectIssueModel source) {
        this(existingIssueDetails, comments, source, null);
    }

    public IssueCommentModel(ExistingIssueDetails<T> existingIssueDetails, List<String> comments, @Nullable ProjectIssueModel source, @Nullable AtlassianDocumentFormatModel atlassianDocumentFormatCommentModel) {
        this.existingIssueDetails = existingIssueDetails;
        this.comments = comments;
        this.source = source;
        this.atlassianDocumentFormatCommentModel = atlassianDocumentFormatCommentModel;
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

}
