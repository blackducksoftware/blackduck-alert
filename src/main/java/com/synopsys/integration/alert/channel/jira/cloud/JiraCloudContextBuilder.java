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
package com.synopsys.integration.alert.channel.jira.cloud;

import com.synopsys.integration.alert.channel.jira.JiraContextBuilder;
import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraDescriptor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.issuetracker.config.IssueTrackerServiceConfig;
import com.synopsys.integration.alert.issuetracker.jira.cloud.JiraProperties;

public class JiraCloudContextBuilder extends JiraContextBuilder {
    @Override
    public String getProjectFieldKey() {
        return JiraDescriptor.KEY_JIRA_PROJECT_NAME;
    }

    @Override
    public String getIssueTypeFieldKey() {
        return JiraDescriptor.KEY_ISSUE_TYPE;
    }

    @Override
    public String getIssueCreatorFieldKey() {
        return JiraDescriptor.KEY_ISSUE_CREATOR;
    }

    @Override
    public String getAddCommentsFieldKey() {
        return JiraDescriptor.KEY_ADD_COMMENTS;
    }

    @Override
    public String getResolveTransitionFieldKey() {
        return JiraDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION;
    }

    @Override
    public String getOpenTransitionFieldKey() {
        return JiraDescriptor.KEY_OPEN_WORKFLOW_TRANSITION;
    }

    @Override
    public String getDefaultIssueCreatorFieldKey() {
        return JiraDescriptor.KEY_JIRA_ADMIN_EMAIL_ADDRESS;
    }

    @Override
    public IssueTrackerServiceConfig createJiraProperties(FieldAccessor fieldAccessor) {
        String url = fieldAccessor.getStringOrNull(JiraDescriptor.KEY_JIRA_URL);
        String username = fieldAccessor.getStringOrNull(JiraDescriptor.KEY_JIRA_ADMIN_EMAIL_ADDRESS);
        String accessToken = fieldAccessor.getStringOrNull(JiraDescriptor.KEY_JIRA_ADMIN_API_TOKEN);
        return new JiraProperties(url, accessToken, username);
    }
}
