/*
 * channel-jira-cloud
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.distribution.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.tracker.event.IssueTrackerCreateIssueEventListener;
import com.synopsys.integration.alert.api.descriptor.JiraCloudChannelKey;

@Component
public class JiraCloudCreateIssueEventListener extends IssueTrackerCreateIssueEventListener {
    @Autowired
    public JiraCloudCreateIssueEventListener(
        Gson gson,
        JiraCloudChannelKey channelKey,
        JiraCloudCreateIssueEventHandler eventHandler
    ) {
        super(gson, channelKey, eventHandler);
    }
}
