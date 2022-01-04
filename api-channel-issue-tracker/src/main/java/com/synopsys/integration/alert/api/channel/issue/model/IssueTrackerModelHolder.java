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

import org.apache.commons.collections4.ListUtils;

public class IssueTrackerModelHolder<T extends Serializable> {
    private final List<IssueCreationModel> issueCreationModels;
    private final List<IssueTransitionModel<T>> issueTransitionModels;
    private final List<IssueCommentModel<T>> issueCommentModels;

    public static <T extends Serializable> IssueTrackerModelHolder<T> reduce(IssueTrackerModelHolder<T> lhs, IssueTrackerModelHolder<T> rhs) {
        List<IssueCreationModel> unifiedIssueCreationModels = ListUtils.union(lhs.getIssueCreationModels(), rhs.getIssueCreationModels());
        List<IssueTransitionModel<T>> unifiedIssueTransitionModels = ListUtils.union(lhs.getIssueTransitionModels(), rhs.getIssueTransitionModels());
        List<IssueCommentModel<T>> unifiedIssueCommentModels = ListUtils.union(lhs.getIssueCommentModels(), rhs.getIssueCommentModels());
        return new IssueTrackerModelHolder<>(unifiedIssueCreationModels, unifiedIssueTransitionModels, unifiedIssueCommentModels);
    }

    public IssueTrackerModelHolder(List<IssueCreationModel> issueCreationModels, List<IssueTransitionModel<T>> issueTransitionModels, List<IssueCommentModel<T>> issueCommentModels) {
        this.issueCreationModels = issueCreationModels;
        this.issueTransitionModels = issueTransitionModels;
        this.issueCommentModels = issueCommentModels;
    }

    public List<IssueCreationModel> getIssueCreationModels() {
        return issueCreationModels;
    }

    public List<IssueTransitionModel<T>> getIssueTransitionModels() {
        return issueTransitionModels;
    }

    public List<IssueCommentModel<T>> getIssueCommentModels() {
        return issueCommentModels;
    }

}
