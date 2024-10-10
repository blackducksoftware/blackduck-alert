/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
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
