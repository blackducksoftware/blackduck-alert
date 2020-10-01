/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.jira.server.web;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.common.JiraConstants;
import com.synopsys.integration.alert.channel.jira.common.util.JiraPluginCheckUtil;
import com.synopsys.integration.alert.channel.jira.server.JiraServerChannelKey;
import com.synopsys.integration.alert.channel.jira.server.JiraServerProperties;
import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.CustomFunctionAction;
import com.synopsys.integration.alert.common.descriptor.DescriptorProcessor;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.FieldValidationUtility;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.rest.service.PluginManagerService;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.exception.IntegrationRestException;

@Component
public class JiraServerCustomFunctionAction extends CustomFunctionAction<String> {
    private final Logger logger = LoggerFactory.getLogger(JiraServerCustomFunctionAction.class);

    private final JiraServerChannelKey jiraChannelKey;
    private final ConfigurationAccessor configurationAccessor;
    private final Gson gson;

    @Autowired
    public JiraServerCustomFunctionAction(AuthorizationManager authorizationManager, JiraServerChannelKey jiraChannelKey, ConfigurationAccessor configurationAccessor, Gson gson, DescriptorProcessor descriptorProcessor,
        FieldValidationUtility fieldValidationUtility) {
        super(JiraServerDescriptor.KEY_JIRA_SERVER_CONFIGURE_PLUGIN, authorizationManager, descriptorProcessor, fieldValidationUtility);
        this.jiraChannelKey = jiraChannelKey;
        this.configurationAccessor = configurationAccessor;
        this.gson = gson;
    }

    @Override
    public ActionResponse<String> createActionResponse(FieldModel fieldModel, HttpServletContentWrapper ignoredServletContent) {
        JiraServerProperties jiraProperties = createJiraProperties(fieldModel);
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
                createBadRequestIntegrationException(e);
            }
            boolean jiraPluginInstalled = JiraPluginCheckUtil.checkIsAppInstalledAndRetryIfNecessary(jiraAppService);
            if (!jiraPluginInstalled) {
                return new ActionResponse<>(HttpStatus.NOT_FOUND, String.format("Unable to confirm Jira server successfully installed the '%s' plugin. Please verify the installation on you Jira server.", JiraConstants.JIRA_ALERT_APP_NAME));
            }
            return new ActionResponse<>(HttpStatus.OK, String.format("Successfully installed the '%s' plugin on Jira server.", JiraConstants.JIRA_ALERT_APP_NAME));
        } catch (IntegrationException e) {
            return createBadRequestIntegrationException(e);
        } catch (InterruptedException e) {
            logger.error("Thread was interrupted while validating Jira plugin installation.", e);
            Thread.currentThread().interrupt();
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Thread was interrupted while validating Jira '%s' plugin installation: %s", JiraConstants.JIRA_ALERT_APP_NAME, e.getMessage()));
        }
    }

    private JiraServerProperties createJiraProperties(FieldModel fieldModel) {
        String url = fieldModel.getFieldValue(JiraServerDescriptor.KEY_SERVER_URL).orElse("");
        String username = fieldModel.getFieldValue(JiraServerDescriptor.KEY_SERVER_USERNAME).orElse("");
        String password = fieldModel.getFieldValueModel(JiraServerDescriptor.KEY_SERVER_PASSWORD)
                              .map(this::getAppropriateAccessToken)
                              .orElse("");
        boolean pluginCheckDisabled = fieldModel.getFieldValue(JiraServerDescriptor.KEY_JIRA_DISABLE_PLUGIN_CHECK).map(Boolean::parseBoolean).orElse(false);

        return new JiraServerProperties(url, password, username, pluginCheckDisabled);
    }

    private String getAppropriateAccessToken(FieldValueModel fieldAccessToken) {
        String accessToken = fieldAccessToken.getValue().orElse("");
        boolean accessTokenSet = fieldAccessToken.getIsSet();
        if (StringUtils.isBlank(accessToken) && accessTokenSet) {
            try {
                return configurationAccessor.getConfigurationsByDescriptorKeyAndContext(jiraChannelKey, ConfigContextEnum.GLOBAL)
                           .stream()
                           .findFirst()
                           .flatMap(configurationModel -> configurationModel.getField(JiraServerDescriptor.KEY_SERVER_PASSWORD))
                           .flatMap(ConfigurationFieldModel::getFieldValue)
                           .orElse("");

            } catch (AlertDatabaseConstraintException e) {
                logger.error("Unable to retrieve existing Jira configuration.");
            }
        }

        return accessToken;
    }

    private ActionResponse<String> createBadRequestIntegrationException(IntegrationException error) {
        logger.error("There was an issue connecting to Jira server", error);
        return new ActionResponse<>(HttpStatus.BAD_REQUEST, "The following error occurred when connecting to Jira server: " + error.getMessage());
    }

}
