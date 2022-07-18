/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.search;

public class IssueTrackerQuery<T> {
    private final T query;

    public IssueTrackerQuery(T query) {
        this.query = query;
    }

    public T getQuery() {
        return query;
    }
}
