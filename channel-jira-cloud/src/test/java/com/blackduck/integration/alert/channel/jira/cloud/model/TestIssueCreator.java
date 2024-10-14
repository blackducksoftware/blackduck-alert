/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.model;

import com.blackduck.integration.jira.common.model.response.UserDetailsResponseModel;

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
