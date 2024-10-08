package com.blackduck.integration.alert.channel.jira.server.model;

import com.blackduck.integration.jira.common.model.components.StatusCategory;
import com.blackduck.integration.jira.common.model.components.StatusDetailsComponent;

public class TestNewStatusDetailsComponent extends StatusDetailsComponent {

    @Override
    public String getName() {
        return "new";
    }

    @Override
    public String getId() {
        return "2";
    }

    @Override
    public StatusCategory getStatusCategory() {
        return new StatusCategory(null, 2, "new", null, "new");
    }
}
