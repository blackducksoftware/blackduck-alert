/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.model;

import java.util.ArrayList;
import java.util.List;

import com.blackduck.integration.jira.common.model.components.IdComponent;
import com.blackduck.integration.jira.common.model.components.IssueFieldsComponent;
import com.blackduck.integration.jira.common.model.response.IssueResponseModel;

public class TestIssueResponse implements IssueResponseModel {
    private final String id;
    private final String key;
    private final String self;
    private final IssueFieldsComponent fields;

    public TestIssueResponse() {
        this("1", "project-1", "https://www.some-url.example.com/rest/api/3/issue/1", null);
    }

    public TestIssueResponse(String id, String key, String self, IssueFieldsComponent fields) {
        this.id = id;
        this.key = key;
        this.self = self;
        this.fields = fields;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getSelf() {
        return self;
    }

    @Override
    public IssueFieldsComponent getFields() {
        return fields;
    }
}
