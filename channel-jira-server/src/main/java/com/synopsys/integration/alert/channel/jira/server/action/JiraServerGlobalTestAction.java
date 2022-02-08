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
import com.synopsys.integration.alert.channel.jira.server.JiraServerProperties;
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
import com.synopsys.integration.jira.common.model.response.MultiPermissionResponseModel;
import com.synopsys.integration.jira.common.model.response.PermissionModel;
import com.synopsys.integration.jira.common.rest.service.PluginManagerService;
import com.synopsys.integration.jira.common.server.model.IssueSearchResponseModel;
import com.synopsys.integration.jira.common.server.service.IssueSearchService;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;
import com.synopsys.integration.jira.common.server.service.MyPermissionsService;

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
        JiraServerProperties jiraProperties = jiraServerPropertiesFactory.createJiraProperties(jiraServerGlobalConfigModel);
        try {
            JiraServerServiceFactory jiraServerServiceFactory = jiraProperties.createJiraServicesServerFactory(logger, gson);
            if (!canUserGetIssues(jiraServerServiceFactory)) {
                return ConfigurationTestResult.failure(TEST_ERROR_MESSAGE + "User does not have access to view any issues in Jira.");
            }

            if (isAppCheckEnabled(jiraServerGlobalConfigModel)) {
                if (!isUserAdmin(jiraServerServiceFactory)) {
                    return ConfigurationTestResult.failure(TEST_ERROR_MESSAGE + "The configured user must be an admin if 'Plugin Check' is enabled");
                }

                if (isAppMissing(jiraServerServiceFactory)) {
                    return ConfigurationTestResult.failure(TEST_ERROR_MESSAGE + String.format("Please configure the '%s' plugin for your server.", JiraConstants.JIRA_ALERT_APP_NAME));
                }
            }
        } catch (IntegrationException ex) {
            return ConfigurationTestResult.failure(TEST_ERROR_MESSAGE + ex.getMessage());
        }
        return ConfigurationTestResult.success(String.format("Successfully connected to %s instance.", JiraServerDescriptor.JIRA_LABEL));
    }


    private boolean isAppCheckEnabled(JiraServerGlobalConfigModel jiraServerGlobalConfigModel) {
        return !jiraServerGlobalConfigModel.getDisablePluginCheck().orElse(false);
    }

    private boolean isAppMissing(JiraServerServiceFactory jiraServerServiceFactory) throws IntegrationException {
        PluginManagerService jiraAppService = jiraServerServiceFactory.createPluginManagerService();
        return !jiraAppService.isAppInstalled(JiraConstants.JIRA_APP_KEY);
    }

    private boolean canUserGetIssues(JiraServerServiceFactory jiraServerServiceFactory) throws IntegrationException {
        IssueSearchService issueSearchService = jiraServerServiceFactory.createIssueSearchService();
        IssueSearchResponseModel issueSearchResponseModel = issueSearchService.queryForIssuePage("", 0, 1);
        return !issueSearchResponseModel.getIssues().isEmpty();
    }

    private boolean isUserAdmin(JiraServerServiceFactory jiraServerServiceFactory) throws IntegrationException {
        MyPermissionsService myPermissionsService = jiraServerServiceFactory.createMyPermissionsService();
        MultiPermissionResponseModel myPermissions = myPermissionsService.getMyPermissions();
        PermissionModel adminPermission = myPermissions.extractPermission(JIRA_ADMIN_PERMISSION_NAME);
        return null != adminPermission && adminPermission.getHavePermission();
    }
}
