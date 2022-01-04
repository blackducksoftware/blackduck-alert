/*
 * channel-jira-cloud
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.web;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.google.api.client.http.HttpStatusCodes;
import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.jira.JiraConstants;
import com.synopsys.integration.alert.api.channel.jira.util.JiraPluginCheckUtils;
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudProperties;
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudPropertiesFactory;
import com.synopsys.integration.alert.channel.jira.cloud.validator.JiraCloudGlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.CustomFunctionAction;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.synopsys.integration.jira.common.rest.service.PluginManagerService;

@Component
public class JiraCloudCustomFunctionAction extends CustomFunctionAction<String> {
    private final Logger logger = LoggerFactory.getLogger(JiraCloudCustomFunctionAction.class);

    private final JiraCloudGlobalConfigurationFieldModelValidator globalConfigurationValidator;
    private final JiraCloudPropertiesFactory jiraCloudPropertiesFactory;
    private final Gson gson;

    @Autowired
    public JiraCloudCustomFunctionAction(AuthorizationManager authorizationManager, JiraCloudGlobalConfigurationFieldModelValidator globalConfigurationValidator, JiraCloudPropertiesFactory jiraCloudPropertiesFactory, Gson gson) {
        super(authorizationManager);
        this.globalConfigurationValidator = globalConfigurationValidator;
        this.jiraCloudPropertiesFactory = jiraCloudPropertiesFactory;
        this.gson = gson;
    }

    @Override
    public ActionResponse<String> createActionResponse(FieldModel fieldModel, HttpServletContentWrapper ignoredServletContent) {
        JiraCloudProperties jiraProperties = jiraCloudPropertiesFactory.createJiraProperties(fieldModel);
        try {
            JiraCloudServiceFactory jiraServicesCloudFactory = jiraProperties.createJiraServicesCloudFactory(logger, gson);
            PluginManagerService jiraAppService = jiraServicesCloudFactory.createPluginManagerService();
            int statusCode = jiraAppService.installMarketplaceCloudApp(JiraConstants.JIRA_APP_KEY);
            if (!HttpStatusCodes.isSuccess(statusCode)) {
                return new ActionResponse<>(HttpStatus.BAD_REQUEST, "The Jira Cloud server responded with error code: " + statusCode);
            }

            boolean jiraPluginInstalled = JiraPluginCheckUtils.checkIsAppInstalledAndRetryIfNecessary(jiraAppService);
            if (!jiraPluginInstalled) {
                return new ActionResponse<>(HttpStatus.NOT_FOUND, String.format(
                    "Unable to confirm successful installation of the Jira Cloud '%s' plugin. Please verify the installation on your Jira Cloud server.", JiraConstants.JIRA_ALERT_APP_NAME));
            }
            return new ActionResponse<>(HttpStatus.OK, String.format("Successfully installed the '%s' plugin on Jira Cloud", JiraConstants.JIRA_ALERT_APP_NAME));
        } catch (IntegrationException e) {
            logger.error("There was an issue connecting to Jira Cloud", e);
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, "The following error occurred when connecting to Jira Cloud: " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("Thread was interrupted while validating jira install.", e);
            Thread.currentThread().interrupt();
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Thread was interrupted while validating Jira '%s' plugin installation: %s", JiraConstants.JIRA_ALERT_APP_NAME, e.getMessage()));
        }
    }

    @Override
    protected Collection<AlertFieldStatus> validateRelatedFields(FieldModel fieldModel) {
        return globalConfigurationValidator.validate(fieldModel);
    }

}
