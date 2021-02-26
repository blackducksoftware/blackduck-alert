/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.actions;

import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudChannel;
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudContextBuilder;
import com.synopsys.integration.alert.channel.jira.common.JiraMessageParser;
import com.synopsys.integration.alert.channel.jira.common.JiraTestIssueRequestCreator;
import com.synopsys.integration.alert.common.channel.ChannelDistributionTestAction;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueTrackerContext;
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class JiraCloudDistributionTestAction extends ChannelDistributionTestAction {
    private final Gson gson;
    private final JiraMessageParser jiraMessageParser;
    private final JiraCloudContextBuilder jiraCloudContextBuilder;

    @Autowired
    public JiraCloudDistributionTestAction(JiraCloudChannel jiraChannel, Gson gson, JiraMessageParser jiraMessageParser, JiraCloudContextBuilder jiraCloudContextBuilder) {
        super(jiraChannel);
        this.gson = gson;
        this.jiraMessageParser = jiraMessageParser;
        this.jiraCloudContextBuilder = jiraCloudContextBuilder;
    }

    @Override
    public MessageResult testConfig(
        DistributionJobModel testJobModel,
        @Nullable ConfigurationModel channelGlobalConfig,
        @Nullable String customTopic,
        @Nullable String customMessage,
        @Nullable String destination
    ) throws IntegrationException {
        if (null == channelGlobalConfig || channelGlobalConfig.isConfiguredFieldsEmpty()) {
            throw new AlertConfigurationException("Missing Jira Cloud global configuration");
        }
        IssueTrackerContext context = jiraCloudContextBuilder.build(channelGlobalConfig, testJobModel);
        JiraTestIssueRequestCreator issueCreator = new JiraTestIssueRequestCreator(jiraMessageParser, customTopic, customMessage);
        JiraCloudCreateIssueTestAction testAction = new JiraCloudCreateIssueTestAction((JiraCloudChannel) getDistributionChannel(), gson, issueCreator);
        return testAction.testConfig(context);
    }

}
