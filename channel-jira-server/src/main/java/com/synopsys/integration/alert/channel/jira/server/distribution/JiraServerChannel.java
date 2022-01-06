/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.issue.IssueTrackerChannel;
import com.synopsys.integration.alert.api.channel.issue.IssueTrackerResponsePostProcessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;

@Component
public class JiraServerChannel extends IssueTrackerChannel<JiraServerJobDetailsModel, String> {
    @Autowired
    protected JiraServerChannel(JiraServerProcessorFactory processorFactory, IssueTrackerResponsePostProcessor responsePostProcessor) {
        super(processorFactory, responsePostProcessor);
    }

}
