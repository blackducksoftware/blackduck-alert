package com.synopsys.integration.alert.channel.jira.server.model;

import com.synopsys.integration.jira.common.model.components.StatusCategory;
import com.synopsys.integration.jira.common.model.components.StatusDetailsComponent;

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
