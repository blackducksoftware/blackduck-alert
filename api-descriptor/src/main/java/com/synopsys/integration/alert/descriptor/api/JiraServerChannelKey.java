/*
 * api-descriptor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.descriptor.api;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.descriptor.api.model.IssueTrackerChannelKey;

@Component
public class JiraServerChannelKey extends IssueTrackerChannelKey {
    private static final String COMPONENT_NAME = "channel_jira_server";
    private static final String JIRA_SERVER_DISPLAY_NAME = "Jira Server";

    public JiraServerChannelKey() {
        super(COMPONENT_NAME, JIRA_SERVER_DISPLAY_NAME);
    }

}
