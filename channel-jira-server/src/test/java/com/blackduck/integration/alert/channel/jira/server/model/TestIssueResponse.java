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
import com.blackduck.integration.jira.common.model.response.IssueResponseModel;

public class TestIssueResponse extends IssueResponseModel {
    private final String id;
    private final String key;
    private final List<IdComponent> transitions;

    public TestIssueResponse() {
        this("1", "project-1", new ArrayList<>());
    }

    public TestIssueResponse(String id, String key, List<IdComponent> transitions) {
        this.id = id;
        this.key = key;
        this.transitions = transitions;
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
    public List<IdComponent> getTransitions() {
        return transitions;
    }
}
