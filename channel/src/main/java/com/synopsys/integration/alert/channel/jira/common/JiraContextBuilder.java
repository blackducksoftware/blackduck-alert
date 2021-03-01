/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.common;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.channel.jira2.common.model.JiraCustomFieldConfig;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraJobCustomFieldModel;

public abstract class JiraContextBuilder<T> {
    public abstract T build(ConfigurationModel channelGlobalConfig, DistributionJobModel testJobModel);

    protected List<JiraCustomFieldConfig> createJiraCustomFieldConfig(Collection<JiraJobCustomFieldModel> jobCustomFieldModels) {
        return jobCustomFieldModels
                   .stream()
                   .map(customField -> new JiraCustomFieldConfig(customField.getFieldName(), customField.getFieldValue()))
                   .collect(Collectors.toList());
    }

}
