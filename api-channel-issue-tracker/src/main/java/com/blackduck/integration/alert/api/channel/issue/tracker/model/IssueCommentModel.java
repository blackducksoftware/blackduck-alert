package com.blackduck.integration.alert.api.channel.issue.tracker.model;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class IssueCommentModel<T extends Serializable> extends AlertSerializableModel {
    private final ExistingIssueDetails<T> existingIssueDetails;
    private final List<String> comments;
    @Nullable
    private final ProjectIssueModel source;

    public IssueCommentModel(ExistingIssueDetails<T> existingIssueDetails, List<String> comments, @Nullable ProjectIssueModel source) {
        this.existingIssueDetails = existingIssueDetails;
        this.comments = comments;
        this.source = source;
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

}
