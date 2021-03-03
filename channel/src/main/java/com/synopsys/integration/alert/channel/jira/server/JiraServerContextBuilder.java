/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
