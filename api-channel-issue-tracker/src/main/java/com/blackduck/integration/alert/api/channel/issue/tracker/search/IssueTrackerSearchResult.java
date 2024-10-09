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
