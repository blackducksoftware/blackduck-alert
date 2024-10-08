/*
 * channel-jira-cloud
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.issue.tracker.IssueTrackerChannel;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;

@Component
public class JiraCloudChannel extends IssueTrackerChannel<JiraCloudJobDetailsModel, String> {
    @Autowired
    public JiraCloudChannel(JiraCloudProcessorFactory jiraCloudProcessorFactory) {
        super(jiraCloudProcessorFactory);
    }

}
