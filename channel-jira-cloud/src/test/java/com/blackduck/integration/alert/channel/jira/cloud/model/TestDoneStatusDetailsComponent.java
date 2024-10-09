package com.blackduck.integration.alert.channel.jira.cloud.model;

import com.blackduck.integration.jira.common.model.components.StatusCategory;
import com.blackduck.integration.jira.common.model.components.StatusDetailsComponent;

public class TestDoneStatusDetailsComponent extends StatusDetailsComponent {
    @Override
    public String getName() {
        return "done";
    }

    @Override
    public String getId() {
        return "1";
    }

    @Override
    public StatusCategory getStatusCategory() {
        return new StatusCategory(null, 1, "done", null, "done");
    }

}
