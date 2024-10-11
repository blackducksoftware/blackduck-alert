/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.model;

import com.blackduck.integration.jira.common.server.model.IssueSearchIssueComponent;

public class TestIssueSearchIssueComponent extends IssueSearchIssueComponent {
    private final String id;
    private final String key;

    public TestIssueSearchIssueComponent() {
        this("1", "project-1");
    }

    public TestIssueSearchIssueComponent(String id, String key) {
        this.id = id;
        this.key = key;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getKey() {
        return key;
    }
}
