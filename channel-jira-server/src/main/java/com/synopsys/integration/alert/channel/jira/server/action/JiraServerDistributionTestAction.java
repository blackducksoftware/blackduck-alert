/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.action;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.issue.action.IssueTrackerTestAction;
import com.synopsys.integration.alert.channel.jira.server.distribution.JiraServerMessageSenderFactory;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;

@Component
public class JiraServerDistributionTestAction extends IssueTrackerTestAction<JiraServerJobDetailsModel, String> {
    @Autowired
    public JiraServerDistributionTestAction(JiraServerChannelKey channelKey, JiraServerMessageSenderFactory messageSenderFactory) {
        super(channelKey, messageSenderFactory);
    }

    @Override
    protected boolean hasResolveTransition(JiraServerJobDetailsModel distributionDetails) {
        return StringUtils.isNotBlank(distributionDetails.getResolveTransition());
    }

    @Override
    protected boolean hasReopenTransition(JiraServerJobDetailsModel distributionDetails) {
        return StringUtils.isNotBlank(distributionDetails.getReopenTransition());
    }

}
