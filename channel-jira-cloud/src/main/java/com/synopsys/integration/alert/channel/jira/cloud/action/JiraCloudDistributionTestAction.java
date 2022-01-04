/*
 * channel-jira-cloud
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.action;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.issue.action.IssueTrackerTestAction;
import com.synopsys.integration.alert.channel.jira.cloud.distribution.JiraCloudMessageSenderFactory;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.JiraCloudChannelKey;

@Component
public class JiraCloudDistributionTestAction extends IssueTrackerTestAction<JiraCloudJobDetailsModel, String> {
    @Autowired
    public JiraCloudDistributionTestAction(JiraCloudChannelKey channelKey, JiraCloudMessageSenderFactory messageSenderFactory) {
        super(channelKey, messageSenderFactory);
    }

    @Override
    protected boolean hasResolveTransition(JiraCloudJobDetailsModel distributionDetails) {
        return StringUtils.isNotBlank(distributionDetails.getResolveTransition());
    }

    @Override
    protected boolean hasReopenTransition(JiraCloudJobDetailsModel distributionDetails) {
        return StringUtils.isNotBlank(distributionDetails.getReopenTransition());
    }
}
