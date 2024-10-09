/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.action;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.issue.tracker.action.IssueTrackerTestAction;
import com.blackduck.integration.alert.api.descriptor.JiraServerChannelKey;
import com.blackduck.integration.alert.channel.jira.server.distribution.JiraServerMessageSenderFactory;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;

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
