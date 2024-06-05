/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.action;

import java.util.UUID;
import java.util.function.Supplier;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.jira.JiraConstants;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.message.model.ConfigurationTestResult;
import com.synopsys.integration.alert.common.rest.api.ConfigurationTestHelper;
import com.synopsys.integration.alert.common.rest.api.ConfigurationValidationHelper;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class JiraServerGlobalTestAction {
    public static final String JIRA_ADMIN_PERMISSION_NAME = "ADMINISTER";

    private static final String TEST_ERROR_MESSAGE = "An error occurred during testing: ";

    private final ConfigurationValidationHelper validationHelper;
    private final ConfigurationTestHelper testHelper;
    private final JiraServerGlobalConfigurationValidator validator;
    private final JiraServerTestActionFactory jiraServerTestActionFactory;
    private final JiraServerGlobalConfigAccessor configurationAccessor;

    @Autowired
    public JiraServerGlobalTestAction(
        AuthorizationManager authorizationManager,
        JiraServerGlobalConfigurationValidator validator,
        JiraServerTestActionFactory jiraServerTestActionFactory,
        JiraServerGlobalConfigAccessor configurationAccessor
    ) {
        this.testHelper = new ConfigurationTestHelper(authorizationManager, ConfigContextEnum.GLOBAL, ChannelKeys.JIRA_SERVER);
        this.validationHelper = new ConfigurationValidationHelper(authorizationManager, ConfigContextEnum.GLOBAL, ChannelKeys.JIRA_SERVER);
        this.validator = validator;
        this.jiraServerTestActionFactory = jiraServerTestActionFactory;
        this.configurationAccessor = configurationAccessor;
    }

    public ActionResponse<ValidationResponseModel> testWithPermissionCheck(JiraServerGlobalConfigModel requestResource) {
        Supplier<ValidationActionResponse> validationSupplier = () -> validationHelper.validate(() -> validator.validate(requestResource, requestResource.getId()));
        return testHelper.test(validationSupplier, () -> testConfigModelContent(requestResource));
    }

    public ConfigurationTestResult testConfigModelContent(JiraServerGlobalConfigModel jiraServerGlobalConfigModel) {
        try {
            if (BooleanUtils.toBoolean(jiraServerGlobalConfigModel.getIsPasswordSet().orElse(Boolean.FALSE)) && jiraServerGlobalConfigModel.getPassword().isEmpty()) {
                configurationAccessor.getConfiguration(UUID.fromString(jiraServerGlobalConfigModel.getId()))
                    .flatMap(JiraServerGlobalConfigModel::getPassword)
                    .ifPresent(jiraServerGlobalConfigModel::setPassword);
            }
            if (BooleanUtils.toBoolean(jiraServerGlobalConfigModel.getIsAccessTokenSet().orElse(Boolean.FALSE)) && jiraServerGlobalConfigModel.getAccessToken().isEmpty()) {
                configurationAccessor.getConfiguration(UUID.fromString(jiraServerGlobalConfigModel.getId()))
                    .flatMap(JiraServerGlobalConfigModel::getAccessToken)
                    .ifPresent(jiraServerGlobalConfigModel::setAccessToken);
            }

            JiraServerGlobalTestActionWrapper testActionWrapper = jiraServerTestActionFactory.createTestActionWrapper(jiraServerGlobalConfigModel);
            if (!testActionWrapper.canUserGetIssues()) {
                return ConfigurationTestResult.failure(TEST_ERROR_MESSAGE + "User does not have access to view any issues in Jira.");
            }

            if (testActionWrapper.isAppCheckEnabled()) {
                if (!testActionWrapper.isUserAdmin()) {
                    return ConfigurationTestResult.failure(TEST_ERROR_MESSAGE + "The configured user must be an admin if 'Plugin Check' is enabled");
                }

                if (testActionWrapper.isAppMissing()) {
                    return ConfigurationTestResult.failure(
                        TEST_ERROR_MESSAGE + String.format("Please configure the '%s' plugin for your server.", JiraConstants.JIRA_ALERT_APP_NAME));
                }
            }
        } catch (IntegrationException ex) {
            return ConfigurationTestResult.failure(TEST_ERROR_MESSAGE + ex.getMessage());
        }
        return ConfigurationTestResult.success(String.format("Successfully connected to %s instance.", JiraServerDescriptor.JIRA_LABEL));
    }
}
