package com.synopsys.integration.issuetracker.jira.server.model;

import com.synopsys.integration.jira.common.model.response.IssueTypeResponseModel;

public class TestIssueTypeResponseModel extends IssueTypeResponseModel {
    private String id;
    private String name;

    public TestIssueTypeResponseModel() {
        this("1", "task");
    }

    public TestIssueTypeResponseModel(final String id, final String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }
}
