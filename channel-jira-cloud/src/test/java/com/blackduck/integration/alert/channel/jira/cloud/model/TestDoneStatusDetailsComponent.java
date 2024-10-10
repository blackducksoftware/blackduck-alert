/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
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
