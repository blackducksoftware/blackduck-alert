/**
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
package com.synopsys.integration.alert.channel.jira.server.descriptor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.PasswordConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.URLInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.EndpointButtonField;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.EncryptionValidator;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;

@Component
public class JiraServerGlobalUIConfig extends UIConfig {
    public static final String LABEL_SERVER_URL = "URL";
    public static final String LABEL_SERVER_USER_NAME = "User Name";
    public static final String LABEL_SERVER_PASSWORD = "Password";
    public static final String LABEL_SERVER_DISABLE_PLUGIN_CHECK = "Disable Plugin Check";
    public static final String LABEL_SERVER_CONFIGURE_PLUGIN = "Configure Jira server plugin";

    public static final String DESCRIPTION_SERVER_URL = "The URL of the Jira server";
    public static final String DESCRIPTION_SERVER_USER_NAME = "The username of the Jira Server user. Note: Unless 'Disable Plugin Check' is checked, this user must be a Jira admin.";
    public static final String DESCRIPTION_SERVER_PASSWORD = "The password of the specified Jira Server user.";
    public static final String DESCRIPTION_SERVER_DISABLE_PLUGIN_CHECK = "This will disable checking whether the 'Alert Issue Property Indexer' plugin is installed on the specified Jira instance."
                                                                             + " Please ensure that the plugin is manually installed before using Alert with Jira. If not, issues created by Alert will not be updated properly.";
    public static final String DESCRIPTION_SERVER_CONFIGURE_PLUGIN = "Installs a required plugin on the Jira server.";

    public static final String BUTTON_LABEL_PLUGIN_CONFIGURATION = "Install Plugin Remotely";

    private final EncryptionValidator encryptionValidator;

    @Autowired
    public JiraServerGlobalUIConfig(EncryptionValidator encryptionValidator) {
        super(JiraServerDescriptor.JIRA_LABEL, JiraServerDescriptor.JIRA_DESCRIPTION, JiraServerDescriptor.JIRA_URL);
        this.encryptionValidator = encryptionValidator;
    }

    @Override
    public List<ConfigField> createFields() {
        ConfigField serverUrlField = new URLInputConfigField(JiraServerDescriptor.KEY_SERVER_URL, LABEL_SERVER_URL, DESCRIPTION_SERVER_URL).applyRequired(true);
        ConfigField jiraUserName = new TextInputConfigField(JiraServerDescriptor.KEY_SERVER_USERNAME, LABEL_SERVER_USER_NAME, DESCRIPTION_SERVER_USER_NAME).applyRequired(true);
        ConfigField jiraPassword = new PasswordConfigField(JiraServerDescriptor.KEY_SERVER_PASSWORD, LABEL_SERVER_PASSWORD, DESCRIPTION_SERVER_PASSWORD, encryptionValidator).applyRequired(true);
        ConfigField jiraDisablePluginCheck = new CheckboxConfigField(JiraServerDescriptor.KEY_JIRA_DISABLE_PLUGIN_CHECK, LABEL_SERVER_DISABLE_PLUGIN_CHECK, DESCRIPTION_SERVER_DISABLE_PLUGIN_CHECK)
                                                 .applyDefaultValue(Boolean.FALSE.toString());

        ConfigField jiraConfigurePlugin = new EndpointButtonField(JiraServerDescriptor.KEY_JIRA_SERVER_CONFIGURE_PLUGIN, LABEL_SERVER_CONFIGURE_PLUGIN, DESCRIPTION_SERVER_CONFIGURE_PLUGIN, BUTTON_LABEL_PLUGIN_CONFIGURATION)
                                              .applyRequiredRelatedField(JiraServerDescriptor.KEY_SERVER_URL)
                                              .applyRequiredRelatedField(JiraServerDescriptor.KEY_SERVER_USERNAME)
                                              .applyRequiredRelatedField(JiraServerDescriptor.KEY_SERVER_PASSWORD);

        return List.of(serverUrlField, jiraUserName, jiraPassword, jiraDisablePluginCheck, jiraConfigurePlugin);
    }

}
