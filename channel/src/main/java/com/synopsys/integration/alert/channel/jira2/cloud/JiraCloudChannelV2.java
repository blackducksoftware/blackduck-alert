/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira2.cloud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.api.issue.IssueTrackerChannel;
import com.synopsys.integration.alert.channel.api.issue.IssueTrackerResponsePostProcessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;

@Component
public class JiraCloudChannelV2 extends IssueTrackerChannel<JiraCloudJobDetailsModel, String> {
    @Autowired
    public JiraCloudChannelV2(JiraCloudProcessorFactory jiraCloudProcessorFactory, IssueTrackerResponsePostProcessor responsePostProcessor) {
        super(jiraCloudProcessorFactory, responsePostProcessor);
    }

}
