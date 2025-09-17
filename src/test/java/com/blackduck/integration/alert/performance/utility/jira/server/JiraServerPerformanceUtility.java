/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.performance.utility.jira.server;

import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.api.descriptor.JiraServerChannelKey;
import com.blackduck.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.blackduck.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.blackduck.integration.alert.common.descriptor.ChannelDescriptor;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.model.FieldValueModel;
import com.blackduck.integration.alert.performance.utility.AlertRequestUtility;
import com.blackduck.integration.alert.performance.utility.ConfigurationManager;
import com.blackduck.integration.alert.performance.utility.IntegrationPerformanceTestRunner;
import com.blackduck.integration.alert.test.common.TestProperties;
import com.blackduck.integration.alert.test.common.TestPropertyKey;
import com.blackduck.integration.exception.IntegrationException;
import com.google.gson.Gson;

public class JiraServerPerformanceUtility {
    private static final String PERFORMANCE_JOB_NAME = "Jira Server Performance Job";
    private static final JiraServerChannelKey CHANNEL_KEY = new JiraServerChannelKey();
    private static final BlackDuckProviderKey PROVIDER_KEY = new BlackDuckProviderKey();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Gson gson = IntegrationPerformanceTestRunner.createGson();
    private final DateTimeFormatter dateTimeFormatter = IntegrationPerformanceTestRunner.createDateTimeFormatter();

    private final AlertRequestUtility alertRequestUtility;
    private final ConfigurationManager configurationManager;

    public JiraServerPerformanceUtility(AlertRequestUtility alertRequestUtility, ConfigurationManager configurationManager) {
        this.alertRequestUtility = alertRequestUtility;
        this.configurationManager = configurationManager;
    }

    public JiraServerGlobalConfigModel createGlobalConfigModelFromProperties(TestProperties testProperties) {
        String createdAt = OffsetDateTime.now().toString();
        String url = testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_URL);
        Integer timeout = Integer.valueOf(testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_TIMEOUT));
        String userName = testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_USERNAME);
        String password = testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_PASSWORD);
        Boolean disablePluginCheck = testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_DISABLE_PLUGIN_CHECK)
            .map(Boolean::valueOf)
            .orElse(Boolean.FALSE);

        return new JiraServerGlobalConfigModel(
            null,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            createdAt,
            createdAt,
            url,
            timeout,
            JiraServerAuthorizationMethod.BASIC,
            userName,
            password,
            Boolean.FALSE,
            null,
            Boolean.FALSE,
            disablePluginCheck
        );
    }

    public Map<String, FieldValueModel> createChannelFieldsMap(TestProperties testProperties, String jobName, String globalConfigId) {
        Map<String, FieldValueModel> channelFieldsMap = new HashMap<>();
        channelFieldsMap.put(ChannelDescriptor.KEY_ENABLED, createFieldValueModel("true"));
        channelFieldsMap.put(ChannelDescriptor.KEY_CHANNEL_NAME, createFieldValueModel(CHANNEL_KEY.getUniversalKey()));
        channelFieldsMap.put(ChannelDescriptor.KEY_NAME, createFieldValueModel(jobName));
        channelFieldsMap.put(ChannelDescriptor.KEY_FREQUENCY, createFieldValueModel(FrequencyType.REAL_TIME.name()));
        channelFieldsMap.put(ChannelDescriptor.KEY_PROVIDER_TYPE, createFieldValueModel(PROVIDER_KEY.getUniversalKey()));
        channelFieldsMap.put(ChannelDescriptor.KEY_CHANNEL_GLOBAL_CONFIG_ID, createFieldValueModel(globalConfigId));

        channelFieldsMap
            .put(JiraServerDescriptor.KEY_ADD_COMMENTS, createFieldValueModel(testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_ADD_COMMENTS).orElse("true")));
        channelFieldsMap
            .put(JiraServerDescriptor.KEY_ISSUE_CREATOR, createFieldValueModel(testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_ISSUE_CREATOR).orElse("")));
        channelFieldsMap
            .put(JiraServerDescriptor.KEY_JIRA_PROJECT_NAME, createFieldValueModel(testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_PROJECT_NAME)));
        channelFieldsMap
            .put(JiraServerDescriptor.KEY_ISSUE_TYPE, createFieldValueModel(testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_ISSUE_TYPE).orElse("Task")));
        channelFieldsMap.put(
            JiraServerDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION,
            createFieldValueModel(testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_RESOLVE_TRANSITION).orElse(""))
        );
        channelFieldsMap.put(
            JiraServerDescriptor.KEY_OPEN_WORKFLOW_TRANSITION,
            createFieldValueModel(testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_REOPEN_TRANSITION).orElse(""))
        );

        return channelFieldsMap;
    }

    public FieldValueModel createFieldValueModel(String value) {
        return new FieldValueModel(List.of(value), true);
    }

    public JiraServerGlobalConfigModel createJiraGlobalConfiguration(JiraServerGlobalConfigModel jiraServerGlobalConfigModel) {
        return createJiraGlobalConfiguration(true, jiraServerGlobalConfigModel);
    }

    public JiraServerGlobalConfigModel createJiraGlobalConfiguration(boolean installPlugin, JiraServerGlobalConfigModel jiraServerGlobalConfigModel) {
        if (installPlugin) {
            ValidationResponseModel installPluginResponse = installPlugin(jiraServerGlobalConfigModel);
            if (installPluginResponse.hasErrors()) {
                fail(String.format("Unable to install the Alert plugin for Jira Server. Error: %s", installPluginResponse.getErrors()));
            }
        }

        Optional<JiraServerGlobalConfigModel> globalConfiguration = createGlobalConfiguration(jiraServerGlobalConfigModel);
        if (globalConfiguration.isEmpty()) {
            fail("Global configuration missing.");
        }
        return globalConfiguration.get();
    }

    public JiraServerGlobalConfigModel createGlobalConfigModel(TestProperties testProperties) {
        String createdAt = OffsetDateTime.now().toString();
        String url = testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_URL);
        Integer timeout = Integer.valueOf(testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_TIMEOUT));
        String userName = testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_USERNAME);
        String password = testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_PASSWORD);
        Boolean disablePluginCheck = testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_DISABLE_PLUGIN_CHECK)
            .map(Boolean::valueOf)
            .orElse(Boolean.FALSE);

        return new JiraServerGlobalConfigModel(
            null,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            createdAt,
            createdAt,
            url,
            timeout,
            JiraServerAuthorizationMethod.BASIC,
            userName,
            password,
            Boolean.FALSE,
            null,
            Boolean.FALSE,
            disablePluginCheck
        );
    }

    private Optional<JiraServerGlobalConfigModel> createGlobalConfiguration(JiraServerGlobalConfigModel jiraServerGlobalConfigModel) {
        String apiConfigurationPath = AlertRestConstants.JIRA_SERVER_CONFIGURATION_PATH;
        if (null != jiraServerGlobalConfigModel) {
            LocalDateTime startingTime = LocalDateTime.now();
            String descriptorName = CHANNEL_KEY.getUniversalKey();
            Optional<JiraServerGlobalConfigModel> globalConfigModel = configurationManager
                .createGlobalConfiguration(apiConfigurationPath, jiraServerGlobalConfigModel, JiraServerGlobalConfigModel.class);
            String globalConfigMessage = String.format("Creating the global Configuration for %s jobs took", descriptorName);
            logTimeElapsedWithMessage(globalConfigMessage + " %s", startingTime, LocalDateTime.now());
            return globalConfigModel;
        }
        return Optional.empty();
    }

    private ValidationResponseModel installPlugin(JiraServerGlobalConfigModel jiraServerGlobalConfigModel) {
        try {
            String requestBody = gson.toJson(jiraServerGlobalConfigModel);
            String installPluginResponseString = alertRequestUtility.executePostRequest(
                String.format("%s/install-plugin", AlertRestConstants.JIRA_SERVER_CONFIGURATION_PATH),
                requestBody,
                "Installing the plugin failed."
            );
            return gson.fromJson(installPluginResponseString, ValidationResponseModel.class);
        } catch (IntegrationException e) {
            logger.error("Unexpected error occurred while installing the plugin.", e);
            return ValidationResponseModel.generalError(e.getMessage());
        }
    }

    public void logTimeElapsedWithMessage(String messageFormat, LocalDateTime start, LocalDateTime end) {
        //TODO log timing to a file
        Duration duration = Duration.between(start, end);
        String durationFormatted = String.format("%sH:%sm:%ss", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        logger.info(String.format(messageFormat, durationFormatted));
        logger.info(String.format("Current time %s.", dateTimeFormatter.format(end)));
    }
}
