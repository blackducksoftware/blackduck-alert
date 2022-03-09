
/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.environment;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.AlertConstants;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.environment.EnvironmentProcessingResult;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandler2;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandlerFactory2;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;

@Component
public class JiraServerEnvironmentVariableHandlerFactory2 implements EnvironmentVariableHandlerFactory2<JiraServerGlobalConfigModel> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String DISABLE_PLUGIN_KEY = "ALERT_CHANNEL_JIRA_SERVER_JIRA_SERVER_DISABLE_PLUGIN_CHECK";
    public static final String PASSWORD_KEY = "ALERT_CHANNEL_JIRA_SERVER_JIRA_SERVER_PASSWORD";
    public static final String URL_KEY = "ALERT_CHANNEL_JIRA_SERVER_JIRA_SERVER_URL";
    public static final String USERNAME_KEY = "ALERT_CHANNEL_JIRA_SERVER_JIRA_SERVER_USERNAME";

    public static final Set<String> VARIABLE_NAMES = Set.of(DISABLE_PLUGIN_KEY, PASSWORD_KEY, URL_KEY, USERNAME_KEY);

    private final JiraServerGlobalConfigAccessor configAccessor;
    private final EnvironmentVariableUtility environmentVariableUtility;
    private final JiraServerGlobalConfigurationValidator validator;

    @Autowired

    public JiraServerEnvironmentVariableHandlerFactory2(JiraServerGlobalConfigAccessor configAccessor, EnvironmentVariableUtility environmentVariableUtility, JiraServerGlobalConfigurationValidator validator) {
        this.configAccessor = configAccessor;
        this.environmentVariableUtility = environmentVariableUtility;
        this.validator = validator;
    }

    @Override
    public EnvironmentVariableHandler2<JiraServerGlobalConfigModel> build() {
        return EnvironmentVariableHandler2.create(
            ChannelKeys.JIRA_SERVER.getDisplayName(),
            VARIABLE_NAMES,
            this::isConfigurationMissing,
            this::updateConfiguration,
            this::validateConfiguration,
            this::configureModel
        );
    }

    private Boolean isConfigurationMissing() {
        return configAccessor.getConfigurationCount() <= 0;
    }

    private JiraServerGlobalConfigModel configureModel() {
        String name = AlertRestConstants.DEFAULT_CONFIGURATION_NAME;
        String url = environmentVariableUtility.getEnvironmentValue(URL_KEY).orElse(null);
        String userName = environmentVariableUtility.getEnvironmentValue(USERNAME_KEY).orElse(null);
        String password = environmentVariableUtility.getEnvironmentValue(PASSWORD_KEY).orElse(null);
        String createdAt = DateUtils.formatDate(DateUtils.createCurrentDateTimestamp(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        JiraServerGlobalConfigModel configModel = new JiraServerGlobalConfigModel(null, name, url, userName, password);
        configModel.setCreatedAt(createdAt);
        configModel.setLastUpdated(createdAt);
        environmentVariableUtility.getEnvironmentValue(DISABLE_PLUGIN_KEY)
            .map(Boolean::valueOf)
            .ifPresent(configModel::setDisablePluginCheck);

        return configModel;
    }

    private ValidationResponseModel validateConfiguration(JiraServerGlobalConfigModel configModel) {
        return validator.validate(configModel);
    }

    private EnvironmentProcessingResult updateConfiguration(JiraServerGlobalConfigModel configModel) {
        EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder(VARIABLE_NAMES);

        JiraServerGlobalConfigModel obfuscatedModel = configModel.obfuscate();
        if (StringUtils.isNotBlank(obfuscatedModel.getUrl())) {
            builder.addVariableValue(URL_KEY, obfuscatedModel.getUrl());
        }

        if (StringUtils.isNotBlank(obfuscatedModel.getUserName())) {
            builder.addVariableValue(USERNAME_KEY, obfuscatedModel.getUserName());
        }

        obfuscatedModel.getDisablePluginCheck()
            .map(String::valueOf)
            .ifPresent(value -> builder.addVariableValue(DISABLE_PLUGIN_KEY, value));

        obfuscatedModel.getIsPasswordSet()
            .filter(Boolean::booleanValue)
            .ifPresent(ignored -> builder.addVariableValue(PASSWORD_KEY, AlertConstants.MASKED_VALUE));

        EnvironmentProcessingResult result = builder.build();
        if (result.hasValues() && configAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME).isEmpty()) {
            try {
                configAccessor.createConfiguration(configModel);
            } catch (AlertConfigurationException ex) {
                logger.error("Failed to create config: ", ex);
            }
        }

        return result;
    }
}
