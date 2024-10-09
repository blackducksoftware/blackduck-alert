/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.search;

import java.io.Serializable;
import java.util.List;

public class IssueTrackerSearchResult<T extends Serializable> {
    private final String searchQuery;
    private final List<ProjectIssueSearchResult<T>> searchResults;

    public IssueTrackerSearchResult(String searchQuery, List<ProjectIssueSearchResult<T>> searchResults) {
        this.searchQuery = searchQuery;
        this.searchResults = searchResults;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public List<ProjectIssueSearchResult<T>> getSearchResults() {
        return searchResults;
    }
}
