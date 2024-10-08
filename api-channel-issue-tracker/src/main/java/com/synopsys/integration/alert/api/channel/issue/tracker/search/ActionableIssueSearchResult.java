/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.tracker.search;

import java.io.Serializable;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;

public class ActionableIssueSearchResult<T extends Serializable> extends AlertSerializableModel {
    private final ExistingIssueDetails<T> existingIssueDetails;
    private final ProjectIssueModel projectIssueModel;
    private final ItemOperation requiredOperation;
    private final String searchQuery;

    public ActionableIssueSearchResult(
        @Nullable ExistingIssueDetails<T> existingIssueDetails,
        ProjectIssueModel projectIssueModel,
        String searchQuery,
        ItemOperation requiredOperation
    ) {
        this.existingIssueDetails = existingIssueDetails;
        this.projectIssueModel = projectIssueModel;
        this.searchQuery = searchQuery;
        this.requiredOperation = requiredOperation;
    }

    public Optional<ExistingIssueDetails<T>> getExistingIssueDetails() {
        return Optional.ofNullable(existingIssueDetails);
    }

    public ProjectIssueModel getProjectIssueModel() {
        return projectIssueModel;
    }

    public ItemOperation getRequiredOperation() {
        return requiredOperation;
    }

    public String getSearchQuery() {
        return searchQuery;
    }
}
