package com.synopsys.integration.alert.jira.server.model;

import java.util.ArrayList;
import java.util.List;

import com.synopsys.integration.jira.common.model.components.IdComponent;
import com.synopsys.integration.jira.common.model.response.IssueResponseModel;

public class TestIssueResponse extends IssueResponseModel {
    private String id;
    private String key;
    private List<IdComponent> transitions;

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
