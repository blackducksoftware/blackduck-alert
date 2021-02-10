/*
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
package com.synopsys.integration.alert.channel.jira2.cloud;

import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopys.integration.alert.channel.api.issue.IssueTrackerMessageSender;
import com.synopys.integration.alert.channel.api.issue.model.IssueCommentModel;
import com.synopys.integration.alert.channel.api.issue.model.IssueCreationModel;
import com.synopys.integration.alert.channel.api.issue.model.IssueTransitionModel;

@Component
public class JiraCloudMessageSender extends IssueTrackerMessageSender<JiraCloudJobDetailsModel, String> {
    @Override
    protected List<IssueTrackerIssueResponseModel> createIssues(JiraCloudJobDetailsModel details, List<IssueCreationModel> issueCreationModels) throws AlertException {
        // FIXME implement
        return List.of();
    }

    @Override
    protected List<IssueTrackerIssueResponseModel> transitionIssues(JiraCloudJobDetailsModel details, List<IssueTransitionModel<String>> issueTransitionModels) throws AlertException {
        // FIXME implement
        return List.of();
    }

    @Override
    protected List<IssueTrackerIssueResponseModel> commentOnIssues(JiraCloudJobDetailsModel details, List<IssueCommentModel<String>> issueCommentModels) throws AlertException {
        // FIXME implement
        return List.of();
    }

}
