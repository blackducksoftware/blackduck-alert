/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.model;

import com.blackduck.integration.jira.common.model.response.IssueTypeResponseModel;

public class TestIssueTypeResponseModel extends IssueTypeResponseModel {
    private final String id;
    private final String name;

    public TestIssueTypeResponseModel() {
        this("1", "task");
    }

    public TestIssueTypeResponseModel(String id, String name) {
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
