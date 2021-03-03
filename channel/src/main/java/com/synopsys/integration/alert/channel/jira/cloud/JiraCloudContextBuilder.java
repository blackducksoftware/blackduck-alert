/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud;

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
        JiraCloudJobDetailsModel jiraCouldJobDetails = distributionJobDetails.getAs(DistributionJobDetailsModel.JIRA_CLOUD);

        JiraIssueConfig issueConfig = new JiraIssueConfig(
            jiraCouldJobDetails.getProjectNameOrKey(),
            jiraCouldJobDetails.getProjectNameOrKey(),
            null,
            jiraCouldJobDetails.getIssueCreatorEmail(),
            jiraCouldJobDetails.getIssueType(),
            jiraCouldJobDetails.isAddComments(),
            jiraCouldJobDetails.getResolveTransition(),
            jiraCouldJobDetails.getReopenTransition(),
            createJiraCustomFieldConfig(jiraCouldJobDetails.getCustomFields())
        );

        return new JiraCloudContext(jiraProperties, issueConfig);
    }

}
