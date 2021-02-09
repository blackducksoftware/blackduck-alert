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
package com.synopsys.integration.alert.channel.jira.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.common.JiraContextBuilder;
import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;

@Component
public class JiraServerContextBuilder extends JiraContextBuilder<JiraServerContext> {
    private final JiraServerPropertiesFactory jiraServerPropertiesFactory;

    @Autowired
    public JiraServerContextBuilder(JiraServerPropertiesFactory jiraServerPropertiesFactory) {
        this.jiraServerPropertiesFactory = jiraServerPropertiesFactory;
    }

    @Override
    protected String getProjectFieldKey() {
        return JiraServerDescriptor.KEY_JIRA_PROJECT_NAME;
    }

    @Override
    protected String getIssueTypeFieldKey() {
        return JiraServerDescriptor.KEY_ISSUE_TYPE;
    }

    @Override
    protected String getIssueCreatorFieldKey() {
        return JiraServerDescriptor.KEY_ISSUE_CREATOR;
    }

    @Override
    protected String getAddCommentsFieldKey() {
        return JiraServerDescriptor.KEY_ADD_COMMENTS;
    }

    @Override
    protected String getResolveTransitionFieldKey() {
        return JiraServerDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION;
    }

    @Override
    protected String getOpenTransitionFieldKey() {
        return JiraServerDescriptor.KEY_OPEN_WORKFLOW_TRANSITION;
    }

    @Override
    protected String getDefaultIssueCreatorFieldKey() {
        return JiraServerDescriptor.KEY_SERVER_USERNAME;
    }

    @Override
    public JiraServerContext build(FieldUtility fieldUtility) {
        return new JiraServerContext(jiraServerPropertiesFactory.createJiraProperties(fieldUtility), createIssueConfig(fieldUtility));
    }

}
