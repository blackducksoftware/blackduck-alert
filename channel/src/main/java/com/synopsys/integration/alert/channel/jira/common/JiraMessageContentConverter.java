/**
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.channel.jira.common;

import javax.annotation.Nullable;

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
