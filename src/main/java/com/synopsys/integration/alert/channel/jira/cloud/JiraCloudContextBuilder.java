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
package com.synopsys.integration.alert.channel.jira.cloud;

import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.jira.cloud.JiraCloudContext;
import com.synopsys.integration.alert.jira.cloud.JiraCloudProperties;
import com.synopsys.integration.alert.jira.common.JiraContextBuilder;

public class JiraCloudContextBuilder extends JiraContextBuilder<JiraCloudContext> {
    @Override
    protected String getProjectFieldKey() {
        return JiraCloudDescriptor.KEY_JIRA_PROJECT_NAME;
    }

    @Override
    protected String getIssueTypeFieldKey() {
        return JiraCloudDescriptor.KEY_ISSUE_TYPE;
    }

    @Override
    protected String getIssueCreatorFieldKey() {
        return JiraCloudDescriptor.KEY_ISSUE_CREATOR;
    }

    @Override
    protected String getAddCommentsFieldKey() {
        return JiraCloudDescriptor.KEY_ADD_COMMENTS;
    }

    @Override
    protected String getResolveTransitionFieldKey() {
        return JiraCloudDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION;
    }

    @Override
    protected String getOpenTransitionFieldKey() {
        return JiraCloudDescriptor.KEY_OPEN_WORKFLOW_TRANSITION;
    }

    @Override
    protected String getDefaultIssueCreatorFieldKey() {
        return JiraCloudDescriptor.KEY_JIRA_ADMIN_EMAIL_ADDRESS;
    }

    @Override
    public JiraCloudContext build(FieldAccessor fieldAccessor) {
        return new JiraCloudContext(createJiraProperties(fieldAccessor), createIssueConfig(fieldAccessor));
    }

    private JiraCloudProperties createJiraProperties(FieldAccessor fieldAccessor) {
        String url = fieldAccessor.getStringOrNull(JiraCloudDescriptor.KEY_JIRA_URL);
        String username = fieldAccessor.getStringOrNull(JiraCloudDescriptor.KEY_JIRA_ADMIN_EMAIL_ADDRESS);
        String accessToken = fieldAccessor.getStringOrNull(JiraCloudDescriptor.KEY_JIRA_ADMIN_API_TOKEN);
        return new JiraCloudProperties(url, accessToken, username);
    }
}
