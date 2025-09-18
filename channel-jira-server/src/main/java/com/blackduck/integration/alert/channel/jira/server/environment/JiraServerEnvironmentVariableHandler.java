/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.environment;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.AlertConstants;
import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.environment.EnvironmentProcessingResult;
import com.blackduck.integration.alert.api.environment.EnvironmentVariableHandler;
import com.blackduck.integration.alert.api.environment.EnvironmentVariableUtility;
import com.blackduck.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.blackduck.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.blackduck.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.blackduck.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationValidator;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.util.DateUtils;

@Component
public class JiraServerEnvironmentVariableHandler extends EnvironmentVariableHandler<JiraServerGlobalConfigModel> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String DISABLE_PLUGIN_KEY = "ALERT_CHANNEL_JIRA_SERVER_JIRA_SERVER_DISABLE_PLUGIN_CHECK";
    public static final String URL_KEY = "ALERT_CHANNEL_JIRA_SERVER_JIRA_SERVER_URL";
    public static final String JIRA_TIMEOUT_KEY = "ALERT_CHANNEL_JIRA_SERVER_JIRA_TIMEOUT";
    public static final String AUTHORIZATION_METHOD_KEY = "ALERT_CHANNEL_JIRA_SERVER_JIRA_SERVER_AUTHORIZATION_METHOD";
    public static final String USERNAME_KEY = "ALERT_CHANNEL_JIRA_SERVER_JIRA_SERVER_USERNAME";
    public static final String PASSWORD_KEY = "ALERT_CHANNEL_JIRA_SERVER_JIRA_SERVER_PASSWORD";
    public static final String ACCESS_TOKEN_KEY = "ALERT_CHANNEL_JIRA_SERVER_JIRA_SERVER_PERSONAL_ACCESS_TOKEN";

    public static final Set<String> VARIABLE_NAMES = Set.of(DISABLE_PLUGIN_KEY, URL_KEY, JIRA_TIMEOUT_KEY, AUTHORIZATION_METHOD_KEY, USERNAME_KEY, PASSWORD_KEY, ACCESS_TOKEN_KEY);

    private final JiraServerGlobalConfigAccessor configAccessor;
    private final EnvironmentVariableUtility environmentVariableUtility;
    private final JiraServerGlobalConfigurationValidator validator;

    @Autowired
    public JiraServerEnvironmentVariableHandler(
        JiraServerGlobalConfigAccessor configAccessor,
        EnvironmentVariableUtility environmentVariableUtility,
        JiraServerGlobalConfigurationValidator validator
    ) {
        super(ChannelKeys.JIRA_SERVER.getDisplayName(), VARIABLE_NAMES, environmentVariableUtility);
        this.configAccessor = configAccessor;
        this.environmentVariableUtility = environmentVariableUtility;
        this.validator = validator;
    }

    @Override
    protected Boolean configurationMissingCheck() {
        return configAccessor.getConfigurationCount() <= 0;
    }

    @Override
    protected JiraServerGlobalConfigModel configureModel() {
        String name = AlertRestConstants.DEFAULT_CONFIGURATION_NAME;
        String url = environmentVariableUtility.getEnvironmentValue(URL_KEY).orElse(null);
        Integer timeout = environmentVariableUtility.getEnvironmentValue(JIRA_TIMEOUT_KEY)
            .filter(NumberUtils::isDigits)
            .map(NumberUtils::toInt)
            .orElse(JiraServerPropertiesFactory.DEFAULT_JIRA_TIMEOUT_SECONDS);
        String userName = environmentVariableUtility.getEnvironmentValue(USERNAME_KEY).orElse(null);
        String password = environmentVariableUtility.getEnvironmentValue(PASSWORD_KEY).orElse(null);
        String createdAt = DateUtils.formatDate(DateUtils.createCurrentDateTimestamp(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        String accessToken = environmentVariableUtility.getEnvironmentValue(ACCESS_TOKEN_KEY).orElse(null);

        JiraServerAuthorizationMethod jiraServerAuthorizationMethod = JiraServerAuthorizationMethod.valueOf(environmentVariableUtility.getEnvironmentValue(AUTHORIZATION_METHOD_KEY)
            .orElse(JiraServerAuthorizationMethod.BASIC.name())
        );

        JiraServerGlobalConfigModel configModel = new JiraServerGlobalConfigModel(null, name, url, timeout, jiraServerAuthorizationMethod);
        configModel.setCreatedAt(createdAt);
        configModel.setLastUpdated(createdAt);
        configModel.setUserName(userName);
        configModel.setPassword(password);
        configModel.setAccessToken(accessToken);
        environmentVariableUtility.getEnvironmentValue(DISABLE_PLUGIN_KEY)
            .map(Boolean::valueOf)
            .ifPresent(configModel::setDisablePluginCheck);

        return configModel;
    }

    @Override
    protected ValidationResponseModel validateConfiguration(JiraServerGlobalConfigModel configModel) {
        return validator.validate(configModel, null);
    }

    @Override
    protected EnvironmentProcessingResult buildProcessingResult(JiraServerGlobalConfigModel obfuscatedConfigModel) {
        EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder(VARIABLE_NAMES);

        if (StringUtils.isNotBlank(obfuscatedConfigModel.getUrl())) {
            builder.addVariableValue(URL_KEY, obfuscatedConfigModel.getUrl());
        }

        if (StringUtils.isNotBlank(obfuscatedConfigModel.getAuthorizationMethod().getDisplayName())) {
            builder.addVariableValue(AUTHORIZATION_METHOD_KEY, obfuscatedConfigModel.getAuthorizationMethod().name());
        }

        obfuscatedConfigModel.getUserName()
            .ifPresent(username -> builder.addVariableValue(USERNAME_KEY, username));

        obfuscatedConfigModel.getDisablePluginCheck()
            .map(String::valueOf)
            .ifPresent(value -> builder.addVariableValue(DISABLE_PLUGIN_KEY, value));

        obfuscatedConfigModel.getIsPasswordSet()
            .filter(Boolean::booleanValue)
            .ifPresent(ignored -> builder.addVariableValue(PASSWORD_KEY, AlertConstants.MASKED_VALUE));

        obfuscatedConfigModel.getIsAccessTokenSet()
            .filter(Boolean::booleanValue)
            .ifPresent(ignored -> builder.addVariableValue(ACCESS_TOKEN_KEY, AlertConstants.MASKED_VALUE));

        return builder.build();
    }

    @Override
    protected void saveConfiguration(JiraServerGlobalConfigModel configModel, EnvironmentProcessingResult processingResult) {
        if (configAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME).isEmpty()) {
            try {
                configAccessor.createConfiguration(configModel);
            } catch (AlertConfigurationException ex) {
                logger.error("Failed to create config: ", ex);
            }
        }
    }
}
