/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.search;

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
