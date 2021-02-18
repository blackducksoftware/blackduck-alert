/*
 * channel-api
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
package com.synopys.integration.alert.channel.api.issue;

import java.io.Serializable;

import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.message.AlertIssueOrigin;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerIssueResponseModel;
import com.synopys.integration.alert.channel.api.issue.model.ExistingIssueDetails;
import com.synopys.integration.alert.channel.api.issue.model.ProjectIssueModel;

public class IssueTrackerIssueResponseCreator<T extends Serializable> {
    private final AlertIssueOriginCreator alertIssueOriginCreator;

    public IssueTrackerIssueResponseCreator(AlertIssueOriginCreator alertIssueOriginCreator) {
        this.alertIssueOriginCreator = alertIssueOriginCreator;
    }

    public final IssueTrackerIssueResponseModel createIssueResponse(ProjectIssueModel source, ExistingIssueDetails<T> existingIssueDetails, IssueOperation issueOperation) {
        AlertIssueOrigin alertIssueOrigin = alertIssueOriginCreator.createIssueOrigin(source);

        return new IssueTrackerIssueResponseModel(
            alertIssueOrigin,
            existingIssueDetails.getIssueKey(),
            existingIssueDetails.getIssueLink(),
            existingIssueDetails.getIssueSummary(),
            issueOperation
        );
    }

}
