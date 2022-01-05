/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.descriptor;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.server.validator.JiraServerDistributionConfigurationValidator;
import com.synopsys.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@Component
public class JiraServerDescriptor extends ChannelDescriptor {
    public static final String JIRA_SERVER_PREFIX = "jira.server.";
    public static final String JIRA_SERVER_CHANNEL_PREFIX = "channel." + JIRA_SERVER_PREFIX;

    public static final String KEY_ADD_COMMENTS = JIRA_SERVER_CHANNEL_PREFIX + "add.comments";
    public static final String KEY_ISSUE_CREATOR = JIRA_SERVER_CHANNEL_PREFIX + "issue.creator";
    public static final String KEY_JIRA_PROJECT_NAME = JIRA_SERVER_CHANNEL_PREFIX + "project.name";
    public static final String KEY_ISSUE_TYPE = JIRA_SERVER_CHANNEL_PREFIX + "issue.type";
    public static final String KEY_RESOLVE_WORKFLOW_TRANSITION = JIRA_SERVER_CHANNEL_PREFIX + "resolve.workflow";
    public static final String KEY_OPEN_WORKFLOW_TRANSITION = JIRA_SERVER_CHANNEL_PREFIX + "reopen.workflow";
    public static final String KEY_FIELD_MAPPING = JIRA_SERVER_CHANNEL_PREFIX + "field.mapping";
    public static final String KEY_ISSUE_SUMMARY = JIRA_SERVER_CHANNEL_PREFIX + "issue.summary";

    public static final String KEY_SERVER_URL = JIRA_SERVER_PREFIX + "url";
    public static final String KEY_SERVER_USERNAME = JIRA_SERVER_PREFIX + "username";
    public static final String KEY_SERVER_PASSWORD = JIRA_SERVER_PREFIX + "password";
    public static final String KEY_JIRA_DISABLE_PLUGIN_CHECK = JIRA_SERVER_PREFIX + "disable.plugin.check";
    public static final String KEY_JIRA_SERVER_CONFIGURE_PLUGIN = JIRA_SERVER_PREFIX + "configure.plugin";

    public static final String JIRA_LABEL = "Jira Server";
    public static final String JIRA_URL = "jira_server";
    public static final String JIRA_DESCRIPTION = "Configure the Jira Server instance that Alert will send issue updates to.";

    public static final String LABEL_FIELD_MAPPING = "Field Mapping";
    public static final String LABEL_ADD_COMMENTS = "Add comments";
    public static final String LABEL_ISSUE_CREATOR = "Issue Creator";
    public static final String LABEL_JIRA_PROJECT = "Jira Project";
    public static final String LABEL_ISSUE_TYPE = "Issue Type";
    public static final String LABEL_RESOLVE_WORKFLOW_TRANSITION = "Resolve Transition";
    public static final String LABEL_OPEN_WORKFLOW_TRANSITION = "Re-open Transition";

    private final JiraServerGlobalConfigurationFieldModelValidator jiraServerGlobalValidator;
    private final JiraServerDistributionConfigurationValidator jiraServerDistributionConfigurationValidator;

    @Autowired
    public JiraServerDescriptor(JiraServerGlobalConfigurationFieldModelValidator jiraServerGlobalValidator, JiraServerDistributionConfigurationValidator jiraServerDistributionConfigurationValidator) {
        super(ChannelKeys.JIRA_SERVER, Set.of(ConfigContextEnum.GLOBAL, ConfigContextEnum.DISTRIBUTION));
        this.jiraServerGlobalValidator = jiraServerGlobalValidator;
        this.jiraServerDistributionConfigurationValidator = jiraServerDistributionConfigurationValidator;
    }

    @Override
    public Optional<GlobalConfigurationFieldModelValidator> getGlobalValidator() {
        return Optional.of(jiraServerGlobalValidator);
    }

    @Override
    public Optional<DistributionConfigurationValidator> getDistributionValidator() {
        return Optional.of(jiraServerDistributionConfigurationValidator);
    }

}
