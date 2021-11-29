package com.synopsys.integration.alert.channel.jira.server.model;

import com.synopsys.integration.jira.common.model.components.StatusCategory;
import com.synopsys.integration.jira.common.model.components.StatusDetailsComponent;

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
