/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira2.server.action.JiraServerDistributionTestAction;
import com.synopsys.integration.alert.common.action.ConfigurationAction;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@Component
public class JiraServerConfigurationAction extends ConfigurationAction {
    @Autowired
    protected JiraServerConfigurationAction(
        JiraServerDistributionTestAction jiraServerDistributionTestAction,
        JiraServerGlobalTestAction jiraServerGlobalTestAction,
        JiraServerJobDetailsExtractor jiraServerJobDetailsExtractor
    ) {
        super(ChannelKeys.JIRA_SERVER);
        addDistributionTestAction(jiraServerDistributionTestAction);
        addGlobalTestAction(jiraServerGlobalTestAction);
        addJobDetailsExtractor(jiraServerJobDetailsExtractor);
    }

}
