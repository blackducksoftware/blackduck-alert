/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.channel.DistributionChannel;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditUtility;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.issuetracker.IssueTrackerContext;
import com.synopsys.integration.alert.issuetracker.jira.server.JiraServerService;
import com.synopsys.integration.alert.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class JiraServerChannel extends DistributionChannel {
    private final JiraServerChannelKey descriptorKey;

    @Autowired
    public JiraServerChannel(Gson gson, JiraServerChannelKey descriptorKey, AuditUtility auditUtility) {
        super(gson, auditUtility);
        this.descriptorKey = descriptorKey;
    }

    @Override
    public MessageResult sendMessage(DistributionEvent event) throws IntegrationException {
        FieldAccessor fieldAccessor = event.getFieldAccessor();
        MessageContentGroup content = event.getContent();
        JiraServerContextBuilder contextBuilder = new JiraServerContextBuilder();
        IssueTrackerContext context = contextBuilder.build(fieldAccessor);
        IssueTrackerRequest request = new IssueTrackerRequest(context, content);
        JiraServerService jiraService = new JiraServerService(getGson());
        IssueTrackerResponse result = jiraService.sendMessage(request);
        return new MessageResult(result.getStatusMessage());
    }

    @Override
    public String getDestinationName() {
        return descriptorKey.getUniversalKey();
    }

}
