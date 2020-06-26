/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.jira.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.common.JiraMessageContentConverter;
import com.synopsys.integration.alert.common.channel.IssueTrackerChannel;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditUtility;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.event.EventManager;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.issuetracker.config.IssueTrackerContext;
import com.synopsys.integration.alert.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.issuetracker.service.IssueTrackerService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.issuetracker.jira.server.JiraServerService;

@Component
public class JiraServerChannel extends IssueTrackerChannel {
    private final JiraMessageContentConverter jiraContentConverter;

    @Autowired
    public JiraServerChannel(Gson gson, JiraServerChannelKey descriptorKey, AuditUtility auditUtility, JiraMessageContentConverter jiraContentConverter, EventManager eventManager) {
        super(gson, auditUtility, descriptorKey, eventManager);
        this.jiraContentConverter = jiraContentConverter;
    }

    @Override
    protected IssueTrackerService<?> getIssueTrackerService() {
        return new JiraServerService(getGson());
    }

    @Override
    protected IssueTrackerContext<?> getIssueTrackerContext(DistributionEvent event) {
        FieldAccessor fieldAccessor = event.getFieldAccessor();
        JiraServerContextBuilder contextBuilder = new JiraServerContextBuilder();
        return contextBuilder.build(fieldAccessor);
    }

    @Override
    protected List<IssueTrackerRequest> createRequests(IssueTrackerContext<?> context, DistributionEvent event) throws IntegrationException {
        return jiraContentConverter.convertMessageContents(context.getIssueConfig(), event.getContent());
    }
}
