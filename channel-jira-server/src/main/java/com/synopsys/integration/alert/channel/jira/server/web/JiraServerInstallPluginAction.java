/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.jira.JiraConstants;
import com.synopsys.integration.alert.api.channel.jira.util.JiraPluginCheckUtils;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.channel.jira.server.JiraServerProperties;
import com.synopsys.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.synopsys.integration.alert.channel.jira.server.action.JiraServerGlobalValidationAction;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.rest.service.PluginManagerService;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.exception.IntegrationRestException;

@Component
public class JiraServerInstallPluginAction {
    private final Logger logger = LoggerFactory.getLogger(JiraServerInstallPluginAction.class);

    private final AuthorizationManager authorizationManager;
    private final JiraServerPropertiesFactory jiraServerPropertiesFactory;
    private final JiraServerGlobalValidationAction jiraServerGlobalValidationAction;
    private final Gson gson;

    public JiraServerInstallPluginAction(
        AuthorizationManager authorizationManager, JiraServerPropertiesFactory jiraServerPropertiesFactory, JiraServerGlobalValidationAction jiraServerGlobalValidationAction,
        Gson gson
    ) {
        this.authorizationManager = authorizationManager;
        this.jiraServerPropertiesFactory = jiraServerPropertiesFactory;
        this.jiraServerGlobalValidationAction = jiraServerGlobalValidationAction;
        this.gson = gson;
    }

    public ActionResponse<ValidationResponseModel> installPlugin(JiraServerGlobalConfigModel jiraServerGlobalConfigModel) {
        if (!authorizationManager.hasExecutePermission(ConfigContextEnum.GLOBAL, ChannelKeys.JIRA_SERVER)) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, ResponseFactory.UNAUTHORIZED_REQUEST_MESSAGE);
        }

        ActionResponse<ValidationResponseModel> validate = jiraServerGlobalValidationAction.validate(jiraServerGlobalConfigModel);
        Boolean validationHasErrors = validate.getContent().map(ValidationResponseModel::hasErrors).orElse(false);
        if (validationHasErrors) {
            return validate;
        }

        JiraServerProperties jiraProperties = jiraServerPropertiesFactory.createJiraProperties(jiraServerGlobalConfigModel);
        try {
            JiraServerServiceFactory jiraServicesFactory = jiraProperties.createJiraServicesServerFactory(logger, gson);
            PluginManagerService jiraAppService = jiraServicesFactory.createPluginManagerService();
            try {
                jiraAppService.installMarketplaceServerApp(JiraConstants.JIRA_APP_KEY);
            } catch (IntegrationRestException e) {
                if (RestConstants.NOT_FOUND_404 == e.getHttpStatusCode()) {
                    return new ActionResponse<>(HttpStatus.NOT_FOUND, String.format(
                        "The marketplace listing of the '%s' app may not support your version of Jira. Please install the app manually or request a compatibility update. Error: %s", JiraConstants.JIRA_ALERT_APP_NAME, e.getMessage()));
                }
                return createBadRequestIntegrationException(e);
            }
            boolean jiraPluginInstalled = JiraPluginCheckUtils.checkIsAppInstalledAndRetryIfNecessary(jiraAppService);
            if (!jiraPluginInstalled) {
                return new ActionResponse<>(HttpStatus.NOT_FOUND, String.format("Unable to confirm Jira server successfully installed the '%s' plugin. Please verify the installation on you Jira server.", JiraConstants.JIRA_ALERT_APP_NAME));
            }
            String successMessage = String.format("Successfully installed the '%s' plugin on Jira server.", JiraConstants.JIRA_ALERT_APP_NAME);
            return new ActionResponse<>(HttpStatus.OK, successMessage, ValidationResponseModel.success(successMessage));
        } catch (IntegrationException e) {
            return createBadRequestIntegrationException(e);
        } catch (InterruptedException e) {
            logger.error("Thread was interrupted while validating Jira plugin installation.", e);
            Thread.currentThread().interrupt();
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Thread was interrupted while validating Jira '%s' plugin installation: %s", JiraConstants.JIRA_ALERT_APP_NAME, e.getMessage()));
        }
    }

    private ActionResponse<ValidationResponseModel> createBadRequestIntegrationException(IntegrationException error) {
        logger.error("There was an issue connecting to Jira server", error);
        ValidationResponseModel validationResponseModel = ValidationResponseModel.generalError("The following error occurred when connecting to Jira server: " + error.getMessage());
        return new ActionResponse<>(HttpStatus.BAD_REQUEST, validationResponseModel);
    }
}
