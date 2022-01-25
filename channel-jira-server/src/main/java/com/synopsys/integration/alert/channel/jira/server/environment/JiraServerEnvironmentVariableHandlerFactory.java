/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.environment;

import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandler;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandlerFactory;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;

@Component
public class JiraServerEnvironmentVariableHandlerFactory implements EnvironmentVariableHandlerFactory {
    public static final String DISABLE_PLUGIN_KEY = "ALERT_CHANNEL_JIRA_SERVER_JIRA_SERVER_DISABLE_PLUGIN_CHECK";
    public static final String PASSWORD_KEY = "ALERT_CHANNEL_JIRA_SERVER_JIRA_SERVER_PASSWORD";
    public static final String URL_KEY = "ALERT_CHANNEL_JIRA_SERVER_JIRA_SERVER_URL";
    public static final String USERNAME_KEY = "ALERT_CHANNEL_JIRA_SERVER_JIRA_SERVER_USERNAME";

    public static final Set<String> VARIABLE_NAMES = Set.of(DISABLE_PLUGIN_KEY, PASSWORD_KEY, URL_KEY, USERNAME_KEY);

    private final JiraServerGlobalConfigAccessor configAccessor;
    private final EnvironmentVariableUtility environmentVariableUtility;

    @Autowired
    public JiraServerEnvironmentVariableHandlerFactory(JiraServerGlobalConfigAccessor configAccessor, EnvironmentVariableUtility environmentVariableUtility) {
        this.configAccessor = configAccessor;
        this.environmentVariableUtility = environmentVariableUtility;
    }

    @Override
    public EnvironmentVariableHandler build() {
        return new EnvironmentVariableHandler(ChannelKeys.JIRA_SERVER.getDisplayName(), VARIABLE_NAMES, this::isConfigurationMissing, this::updateConfiguration);
    }

    private Boolean isConfigurationMissing() {
        return configAccessor.getConfigurationCount() <= 0;
    }

    private Properties updateConfiguration() {
        Properties properties = new Properties();
        String url = environmentVariableUtility.getEnvironmentValue(URL_KEY).orElse(null);

        if (StringUtils.isBlank(url)) {
            return properties;
        }

        String name = AlertRestConstants.DEFAULT_CONFIGURATION_NAME;
        String userName = environmentVariableUtility.getEnvironmentValue(USERNAME_KEY).orElse(null);
        String createdAt = DateUtils.formatDate(DateUtils.createCurrentDateTimestamp(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        JiraServerGlobalConfigModel configModel = new JiraServerGlobalConfigModel(null, name, createdAt, createdAt, url, userName);
        environmentVariableUtility.getEnvironmentValue(DISABLE_PLUGIN_KEY)
            .map(Boolean::valueOf)
            .ifPresent(configModel::setDisablePluginCheck);

        environmentVariableUtility.getEnvironmentValue(PASSWORD_KEY)
            .ifPresent(configModel::setPassword);

        JiraServerGlobalConfigModel obfuscatedModel = configModel.obfuscate();
        properties.put(URL_KEY, obfuscatedModel.getUrl());
        if (StringUtils.isNotBlank(obfuscatedModel.getUserName())) {
            properties.put(USERNAME_KEY, obfuscatedModel.getUserName());
        }
        obfuscatedModel.getDisablePluginCheck().map(String::valueOf).ifPresent(value -> properties.put(DISABLE_PLUGIN_KEY, value));
        Boolean passwordSet = obfuscatedModel.getPasswordSet().orElse(Boolean.FALSE);

        if (Boolean.TRUE.equals(passwordSet)) {
            properties.put(PASSWORD_KEY, AlertRestConstants.MASKED_VALUE);
        }

        if (!properties.isEmpty() && configAccessor.getConfigurationByName(name).isEmpty()) {
            configAccessor.createConfiguration(configModel);
        }

        return properties;
    }
}
