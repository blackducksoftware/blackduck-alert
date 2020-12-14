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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.common.JiraContextBuilder;
import com.synopsys.integration.alert.channel.jira.common.model.JiraIssueConfig;
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
    public JiraCloudContext build(ConfigurationModel globalConfig, DistributionJobModel jobModel) {
        FieldUtility globalFieldUtility = new FieldUtility(globalConfig.getCopyOfKeyToFieldMap());
        JiraCloudProperties jiraProperties = jiraCloudPropertiesFactory.createJiraProperties(globalFieldUtility);

        DistributionJobDetailsModel distributionJobDetails = jobModel.getDistributionJobDetails();
        JiraCloudJobDetailsModel jiraCouldJobDetails = distributionJobDetails.getAsJiraCouldJobDetails();

        // FIXME add custom fields
        JiraIssueConfig issueConfig = new JiraIssueConfig(
            jiraCouldJobDetails.getProjectNameOrKey(),
            jiraCouldJobDetails.getProjectNameOrKey(),
            null,
            jiraCouldJobDetails.getIssueCreatorEmail(),
            jiraCouldJobDetails.getIssueType(),
            jiraCouldJobDetails.isAddComments(),
            jiraCouldJobDetails.getResolveTransition(),
            jiraCouldJobDetails.getReopenTransition(),
            List.of()
        );

        return new JiraCloudContext(jiraProperties, issueConfig);
    }

}
