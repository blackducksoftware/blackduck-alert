/**
 * channel
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.synopsys.integration.alert.channel.jira.common.JiraContextBuilder;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;

@Component
public class JiraCloudContextBuilder extends JiraContextBuilder<JiraCloudContext> {
    private final JiraCloudPropertiesFactory jiraCloudPropertiesFactory;

    @Autowired
    public JiraCloudContextBuilder(JiraCloudPropertiesFactory jiraCloudPropertiesFactory) {
        this.jiraCloudPropertiesFactory = jiraCloudPropertiesFactory;
    }

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
    public JiraCloudContext build(FieldUtility fieldUtility) {
        return new JiraCloudContext(jiraCloudPropertiesFactory.createJiraProperties(fieldUtility), createIssueConfig(fieldUtility));
    }

    @Override
    public JiraCloudContext build(ConfigurationModel globalConfig, DistributionJobModel testJobModel) {
        FieldUtility globalFieldUtility = new FieldUtility(globalConfig.getCopyOfKeyToFieldMap());
        JiraCloudProperties jiraProperties = jiraCloudPropertiesFactory.createJiraProperties(globalFieldUtility);

        DistributionJobDetailsModel distributionJobDetails = testJobModel.getDistributionJobDetails();
        JiraCloudJobDetailsModel jiraCouldJobDetails = distributionJobDetails.getAsJiraCouldJobDetails();

        IssueConfig issueConfig = new IssueConfig();
        issueConfig.setProjectName(jiraCouldJobDetails.getProjectNameOrKey());
        issueConfig.setIssueCreator(jiraCouldJobDetails.getIssueCreatorEmail());
        issueConfig.setIssueType(jiraCouldJobDetails.getIssueType());
        issueConfig.setCommentOnIssues(jiraCouldJobDetails.isAddComments());
        issueConfig.setResolveTransition(jiraCouldJobDetails.getResolveTransition());
        issueConfig.setOpenTransition(jiraCouldJobDetails.getReopenTransition());

        return new JiraCloudContext(jiraProperties, issueConfig);
    }

}
