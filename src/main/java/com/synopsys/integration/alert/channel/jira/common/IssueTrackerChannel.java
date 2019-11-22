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
package com.synopsys.integration.alert.channel.jira.common;

import java.util.List;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.channel.DistributionChannel;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditUtility;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.issuetracker.config.IssueTrackerContext;
import com.synopsys.integration.alert.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.alert.issuetracker.service.IssueTrackerService;
import com.synopsys.integration.exception.IntegrationException;

//TODO move into alert-common when SetMap is in integration-common
public abstract class IssueTrackerChannel extends DistributionChannel {
    private final DescriptorKey descriptorKey;

    public IssueTrackerChannel(Gson gson, AuditUtility auditUtility, DescriptorKey descriptorKey) {
        super(gson, auditUtility);
        this.descriptorKey = descriptorKey;
    }

    protected abstract IssueTrackerService<?> getIssueTrackerService();

    protected abstract IssueTrackerContext<?> getIssueTrackerContext(DistributionEvent event);

    protected abstract List<IssueTrackerRequest> createRequests(IssueTrackerContext<?> context, DistributionEvent event) throws IntegrationException;

    @Override
    public MessageResult sendMessage(DistributionEvent event) throws IntegrationException {
        IssueTrackerContext context = getIssueTrackerContext(event);
        IssueTrackerService service = getIssueTrackerService();
        List<IssueTrackerRequest> requests = createRequests(context, event);
        IssueTrackerResponse result = service.sendRequests(context, requests);
        return new MessageResult(result.getStatusMessage());
    }

    @Override
    public String getDestinationName() {
        return descriptorKey.getUniversalKey();
    }
}
