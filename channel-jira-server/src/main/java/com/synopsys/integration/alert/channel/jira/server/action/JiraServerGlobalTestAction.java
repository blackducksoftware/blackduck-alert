/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.action;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.jira.JiraConstants;
import com.synopsys.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.message.model.ConfigurationTestResult;
import com.synopsys.integration.alert.common.rest.api.ConfigurationTestHelper;
import com.synopsys.integration.alert.common.rest.api.ConfigurationValidationHelper;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class JiraServerGlobalTestAction {
    public static final String JIRA_ADMIN_PERMISSION_NAME = "ADMINISTER";

    private static final String TEST_ERROR_MESSAGE = "An error occurred during testing: ";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ConfigurationValidationHelper validationHelper;
    private final ConfigurationTestHelper testHelper;
    private final JiraServerGlobalConfigurationValidator validator;
    private final JiraServerPropertiesFactory jiraServerPropertiesFactory;
    private final Gson gson;

    @Autowired
    public JiraServerGlobalTestAction(AuthorizationManager authorizationManager, JiraServerGlobalConfigurationValidator validator, JiraServerPropertiesFactory jiraServerPropertiesFactory, Gson gson) {
        this.testHelper = new ConfigurationTestHelper(authorizationManager, ConfigContextEnum.GLOBAL, ChannelKeys.JIRA_SERVER);
        this.validationHelper = new ConfigurationValidationHelper(authorizationManager, ConfigContextEnum.GLOBAL, ChannelKeys.JIRA_SERVER);
        this.validator = validator;
        this.jiraServerPropertiesFactory = jiraServerPropertiesFactory;
        this.gson = gson;
    }

    public ActionResponse<ValidationResponseModel> testWithPermissionCheck(JiraServerGlobalConfigModel requestResource) {
        Supplier<ValidationActionResponse> validationSupplier = () -> validationHelper.validate(() -> validator.validate(requestResource));
        return testHelper.test(validationSupplier, () -> testConfigModelContent(requestResource));
    }

    public ConfigurationTestResult testConfigModelContent(JiraServerGlobalConfigModel jiraServerGlobalConfigModel) {
        try {
            JiraServerGlobalTestActionWrapper testActionWrapper = new JiraServerGlobalTestActionWrapper(jiraServerPropertiesFactory, gson, jiraServerGlobalConfigModel);
            if (!testActionWrapper.canUserGetIssues()) {
                return ConfigurationTestResult.failure(TEST_ERROR_MESSAGE + "User does not have access to view any issues in Jira.");
            }

            if (testActionWrapper.isAppCheckEnabled()) {
                if (!testActionWrapper.isUserAdmin()) {
                    return ConfigurationTestResult.failure(TEST_ERROR_MESSAGE + "The configured user must be an admin if 'Plugin Check' is enabled");
                }

                if (testActionWrapper.isAppMissing()) {
                    return ConfigurationTestResult.failure(TEST_ERROR_MESSAGE + String.format("Please configure the '%s' plugin for your server.", JiraConstants.JIRA_ALERT_APP_NAME));
                }
            }
        } catch (IntegrationException ex) {
            return ConfigurationTestResult.failure(TEST_ERROR_MESSAGE + ex.getMessage());
        }
        return ConfigurationTestResult.success(String.format("Successfully connected to %s instance.", JiraServerDescriptor.JIRA_LABEL));
    }
}
