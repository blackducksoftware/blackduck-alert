/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.descriptor;

import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.model.IssueTrackerChannelKey;

@Component
public final class JiraCloudChannelKey extends IssueTrackerChannelKey {
    private static final String COMPONENT_NAME = "channel_jira_cloud";
    private static final String JIRA_CLOUD_DISPLAY_NAME = "Jira Cloud";

    public JiraCloudChannelKey() {
        super(COMPONENT_NAME, JIRA_CLOUD_DISPLAY_NAME);
    }

}
