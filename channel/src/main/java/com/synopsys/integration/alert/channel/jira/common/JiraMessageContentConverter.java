/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.common;

import org.jetbrains.annotations.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.common.util.JiraIssuePropertiesUtil;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueSearchProperties;
import com.synopsys.integration.alert.common.channel.issuetracker.service.IssueTrackerRequestCreator;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

@Component
public class JiraMessageContentConverter extends IssueTrackerRequestCreator {
    @Autowired
    public JiraMessageContentConverter(JiraMessageParser jiraMessageParser) {
        super(jiraMessageParser);
    }

    @Override
    protected IssueSearchProperties createIssueSearchProperties(String providerName, String providerUrl, LinkableItem topic, @Nullable LinkableItem subTopic, @Nullable ComponentItem componentItem, String additionalInfo) {
        return JiraIssuePropertiesUtil.create(providerName, providerUrl, topic, subTopic, componentItem, additionalInfo);
    }

}
