/*
 * channel-jira-cloud
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.descriptor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.PasswordConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.URLInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.EndpointButtonField;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.EncryptionSettingsValidator;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;

@Component
public class JiraCloudGlobalUIConfig extends UIConfig {
    public static final String LABEL_URL = "URL";
    public static final String LABEL_EMAIL_ADDRESS = "Email Address";
    public static final String LABEL_API_TOKEN = "API Token";
    public static final String LABEL_DISABLE_PLUGIN_CHECK = "Disable Plugin Check";
    public static final String LABEL_CONFIGURE_PLUGIN = "Configure Jira Cloud plugin";

    public static final String DESCRIPTION_URL = "The URL of the Jira Cloud server.";
    public static final String DESCRIPTION_USER_NAME = "The email address of the Jira Cloud user. Note: Unless 'Disable Plugin Check' is checked, this user must be a Jira admin.";
    public static final String DESCRIPTION_API_TOKEN = "The API token of the specified Jira user.";
    public static final String DESCRIPTION_DISABLE_PLUGIN_CHECK = "This will disable checking whether the 'Alert Issue Property Indexer' plugin is installed on the specified Jira instance."
                                                                      + " Please ensure that the plugin is manually installed before using Alert with Jira."
                                                                      + " If not, issues created by Alert will not be updated properly, and duplicate issues may be created.";
    public static final String DESCRIPTION_CONFIGURE_PLUGIN = "Installs a required plugin on the Jira Cloud server.";

    public static final String BUTTON_LABEL_PLUGIN_CONFIGURATION = "Install Plugin Remotely";

    private final EncryptionSettingsValidator encryptionValidator;

    @Autowired
    public JiraCloudGlobalUIConfig(EncryptionSettingsValidator encryptionValidator) {
        super(JiraCloudDescriptor.JIRA_LABEL, JiraCloudDescriptor.JIRA_DESCRIPTION, JiraCloudDescriptor.JIRA_URL);
        this.encryptionValidator = encryptionValidator;
    }

    @Override
    public List<ConfigField> createFields() {
        ConfigField jiraUrl = new URLInputConfigField(JiraCloudDescriptor.KEY_JIRA_URL, LABEL_URL, DESCRIPTION_URL).applyRequired(true);
        ConfigField jiraUserName = new TextInputConfigField(JiraCloudDescriptor.KEY_JIRA_ADMIN_EMAIL_ADDRESS, LABEL_EMAIL_ADDRESS, DESCRIPTION_USER_NAME).applyRequired(true);
        ConfigField jiraAccessToken = new PasswordConfigField(JiraCloudDescriptor.KEY_JIRA_ADMIN_API_TOKEN, LABEL_API_TOKEN, DESCRIPTION_API_TOKEN, encryptionValidator).applyRequired(true);
        ConfigField jiraDisablePluginCheck = new CheckboxConfigField(JiraCloudDescriptor.KEY_JIRA_DISABLE_PLUGIN_CHECK, LABEL_DISABLE_PLUGIN_CHECK, DESCRIPTION_DISABLE_PLUGIN_CHECK).applyDefaultValue(Boolean.FALSE.toString());
        ConfigField jiraConfigurePlugin = new EndpointButtonField(JiraCloudDescriptor.KEY_JIRA_CONFIGURE_PLUGIN, LABEL_CONFIGURE_PLUGIN, DESCRIPTION_CONFIGURE_PLUGIN, BUTTON_LABEL_PLUGIN_CONFIGURATION)
                                              .applyRequiredRelatedField(JiraCloudDescriptor.KEY_JIRA_URL)
                                              .applyRequiredRelatedField(JiraCloudDescriptor.KEY_JIRA_ADMIN_EMAIL_ADDRESS)
                                              .applyRequiredRelatedField(JiraCloudDescriptor.KEY_JIRA_ADMIN_API_TOKEN);

        return List.of(jiraUrl, jiraUserName, jiraAccessToken, jiraDisablePluginCheck, jiraConfigurePlugin);
    }

}
