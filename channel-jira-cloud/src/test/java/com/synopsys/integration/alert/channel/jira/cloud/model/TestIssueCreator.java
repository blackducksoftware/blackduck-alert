package com.synopsys.integration.alert.channel.jira.cloud.model;

import com.synopsys.integration.jira.common.model.response.UserDetailsResponseModel;

public class TestIssueCreator extends UserDetailsResponseModel {
    private final String creator;

    public TestIssueCreator() {
        this("creator");
    }

    public TestIssueCreator(String creator) {
        this.creator = creator;
    }

    @Override
    public String getName() {
        return creator;
    }

    @Override
    public String getKey() {
        return creator;
    }

    @Override
    public String getAccountId() {
        return creator;
    }

    @Override
    public String getEmailAddress() {
        return creator;
    }
}
