package com.synopsys.integration.issuetracker.jira.server.model;

import com.synopsys.integration.jira.common.server.model.IssueSearchIssueComponent;

public class TestIssueSearchIssueComponent extends IssueSearchIssueComponent {
    private String id;
    private String key;

    public TestIssueSearchIssueComponent() {
        this("1", "project-1");
    }

    public TestIssueSearchIssueComponent(final String id, final String key) {
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
