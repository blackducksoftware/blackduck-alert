package com.blackduck.integration.alert.api.channel.issue.tracker.search;

public class IssueTrackerQuery<T> {
    private final T query;

    public IssueTrackerQuery(T query) {
        this.query = query;
    }

    public T getQuery() {
        return query;
    }
}
