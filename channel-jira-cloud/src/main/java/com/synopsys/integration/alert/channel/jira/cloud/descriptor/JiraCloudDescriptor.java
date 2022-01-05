/*
 * channel-jira-cloud
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.descriptor;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.cloud.validator.JiraCloudDistributionConfigurationValidator;
import com.synopsys.integration.alert.channel.jira.cloud.validator.JiraCloudGlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@Component
public class JiraCloudDescriptor extends ChannelDescriptor {
    public static final String JIRA_CLOUD_PREFIX = "jira.cloud.";
    public static final String JIRA_CLOUD_CHANNEL_PREFIX = "channel." + JIRA_CLOUD_PREFIX;

    public static final String KEY_JIRA_URL = JIRA_CLOUD_PREFIX + "url";
    public static final String KEY_JIRA_ADMIN_EMAIL_ADDRESS = JIRA_CLOUD_PREFIX + "admin.email.address";
    public static final String KEY_JIRA_ADMIN_API_TOKEN = JIRA_CLOUD_PREFIX + "admin.api.token";
    public static final String KEY_JIRA_DISABLE_PLUGIN_CHECK = JIRA_CLOUD_PREFIX + "disable.plugin.check";
    public static final String KEY_JIRA_CONFIGURE_PLUGIN = JIRA_CLOUD_PREFIX + "configure.plugin";

    public static final String KEY_ADD_COMMENTS = JIRA_CLOUD_CHANNEL_PREFIX + "add.comments";
    public static final String KEY_ISSUE_CREATOR = JIRA_CLOUD_CHANNEL_PREFIX + "issue.creator";
    public static final String KEY_JIRA_PROJECT_NAME = JIRA_CLOUD_CHANNEL_PREFIX + "project.name";
    public static final String KEY_ISSUE_TYPE = JIRA_CLOUD_CHANNEL_PREFIX + "issue.type";
    public static final String KEY_RESOLVE_WORKFLOW_TRANSITION = JIRA_CLOUD_CHANNEL_PREFIX + "resolve.workflow";
    public static final String KEY_OPEN_WORKFLOW_TRANSITION = JIRA_CLOUD_CHANNEL_PREFIX + "reopen.workflow";
    public static final String KEY_FIELD_MAPPING = JIRA_CLOUD_CHANNEL_PREFIX + "field.mapping";
    public static final String KEY_ISSUE_SUMMARY = JIRA_CLOUD_CHANNEL_PREFIX + "issue.summary";

    public static final String JIRA_LABEL = "Jira Cloud";
    public static final String JIRA_URL = "jira";
    public static final String JIRA_DESCRIPTION = "Configure the Jira Cloud instance that Alert will send issue updates to.";

    public static final String LABEL_ADD_COMMENTS = "Add Comments";
    public static final String LABEL_ISSUE_CREATOR = "Issue Creator";
    public static final String LABEL_JIRA_PROJECT = "Jira Project";
    public static final String LABEL_ISSUE_TYPE = "Issue Type";
    public static final String LABEL_RESOLVE_WORKFLOW_TRANSITION = "Resolve Transition";
    public static final String LABEL_OPEN_WORKFLOW_TRANSITION = "Re-open Transition";
    public static final String LABEL_FIELD_MAPPING = "Field Mapping";

    private final JiraCloudGlobalConfigurationFieldModelValidator jiraCloudGlobalValidator;
    private final JiraCloudDistributionConfigurationValidator jiraCloudDistributionConfigurationValidator;

    @Autowired
    public JiraCloudDescriptor(JiraCloudGlobalConfigurationFieldModelValidator jiraCloudGlobalValidator, JiraCloudDistributionConfigurationValidator jiraCloudDistributionConfigurationValidator) {
        super(ChannelKeys.JIRA_CLOUD, Set.of(ConfigContextEnum.GLOBAL, ConfigContextEnum.DISTRIBUTION));
        this.jiraCloudGlobalValidator = jiraCloudGlobalValidator;
        this.jiraCloudDistributionConfigurationValidator = jiraCloudDistributionConfigurationValidator;
    }

    @Override
    public Optional<GlobalConfigurationFieldModelValidator> getGlobalValidator() {
        return Optional.of(jiraCloudGlobalValidator);
    }

    @Override
    public Optional<DistributionConfigurationValidator> getDistributionValidator() {
        return Optional.of(jiraCloudDistributionConfigurationValidator);
    }

}
