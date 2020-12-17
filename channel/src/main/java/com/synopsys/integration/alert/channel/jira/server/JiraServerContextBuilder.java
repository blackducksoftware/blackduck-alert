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
package com.synopsys.integration.alert.channel.jira.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.common.JiraContextBuilder;
import com.synopsys.integration.alert.channel.jira.common.model.JiraIssueConfig;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;

@Component
public class JiraServerContextBuilder extends JiraContextBuilder<JiraServerContext> {
    private final JiraServerPropertiesFactory jiraServerPropertiesFactory;

    @Autowired
    public JiraServerContextBuilder(JiraServerPropertiesFactory jiraServerPropertiesFactory) {
        this.jiraServerPropertiesFactory = jiraServerPropertiesFactory;
    }

    @Override
    public JiraServerContext build(ConfigurationModel globalConfig, DistributionJobModel testJobModel) {
        FieldUtility globalFieldUtility = new FieldUtility(globalConfig.getCopyOfKeyToFieldMap());
        JiraServerProperties jiraProperties = jiraServerPropertiesFactory.createJiraProperties(globalFieldUtility);

        DistributionJobDetailsModel distributionJobDetails = testJobModel.getDistributionJobDetails();
        JiraServerJobDetailsModel jiraServerJobDetails = distributionJobDetails.getAs(DistributionJobDetailsModel.JIRA_SERVER);

        JiraIssueConfig issueConfig = new JiraIssueConfig(
            jiraServerJobDetails.getProjectNameOrKey(),
            jiraServerJobDetails.getProjectNameOrKey(),
            null,
            jiraServerJobDetails.getIssueCreatorUsername(),
            jiraServerJobDetails.getIssueType(),
            jiraServerJobDetails.isAddComments(),
            jiraServerJobDetails.getResolveTransition(),
            jiraServerJobDetails.getReopenTransition(),
            createJiraCustomFieldConfig(jiraServerJobDetails.getCustomFields())
        );
        return new JiraServerContext(jiraProperties, issueConfig);
    }

}
