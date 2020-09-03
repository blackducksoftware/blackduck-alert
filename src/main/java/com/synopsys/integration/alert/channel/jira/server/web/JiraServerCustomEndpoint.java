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

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.common.JiraConstants;
import com.synopsys.integration.alert.channel.jira.server.JiraServerChannelKey;
import com.synopsys.integration.alert.channel.jira.server.JiraServerProperties;
import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.common.action.ActionResult;
import com.synopsys.integration.alert.common.action.endpoint.CustomEndpoint;
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
public class JiraServerCustomEndpoint extends CustomEndpoint<String> {
    private final Logger logger = LoggerFactory.getLogger(JiraServerCustomEndpoint.class);

    private final JiraServerChannelKey jiraChannelKey;
    private final ConfigurationAccessor configurationAccessor;
    private final Gson gson;

    @Autowired
    public JiraServerCustomEndpoint(AuthorizationManager authorizationManager, JiraServerChannelKey jiraChannelKey, ConfigurationAccessor configurationAccessor, Gson gson) {
        super(authorizationManager);
        this.jiraChannelKey = jiraChannelKey;
        this.configurationAccessor = configurationAccessor;
        this.gson = gson;
    }

    @Override
    public ActionResult<String> createActionResponse(FieldModel fieldModel, HttpServletContentWrapper ignoredServletContent) {
        JiraServerProperties jiraProperties = createJiraProperties(fieldModel);
        try {
            JiraServerServiceFactory jiraServicesFactory = jiraProperties.createJiraServicesServerFactory(logger, gson);
            PluginManagerService jiraAppService = jiraServicesFactory.createPluginManagerService();
            String username = jiraProperties.getUsername();
            String password = jiraProperties.getPassword();
            try {
                jiraAppService.installMarketplaceServerApp(JiraConstants.JIRA_APP_KEY, username, password);
            } catch (IntegrationRestException e) {
                if (RestConstants.NOT_FOUND_404 == e.getHttpStatusCode()) {
                    return new ActionResult<>(HttpStatus.NOT_FOUND,
                        "The marketplace listing of the Alert Issue Property Indexer app may not support your version of Jira. Please install the app manually or request a compatibility update. Error: " + e.getMessage());
                }
                createBadRequestIntegrationException(e);
            }
            boolean jiraPluginInstalled = isJiraPluginInstalled(jiraAppService, password, username, JiraConstants.JIRA_APP_KEY);
            if (!jiraPluginInstalled) {
                return new ActionResult<>(HttpStatus.NOT_FOUND, "Was not able to confirm Jira server successfully installed the Jira Server plugin. Please verify the installation on you Jira server.");
            }
            return new ActionResult<>(HttpStatus.OK, "Successfully installed the Alert plugin on Jira server.");
        } catch (IntegrationException e) {
            return createBadRequestIntegrationException(e);
        } catch (InterruptedException e) {
            logger.error("Thread was interrupted while validating jira install.", e);
            Thread.currentThread().interrupt();
            return new ActionResult<>(HttpStatus.INTERNAL_SERVER_ERROR, "Thread was interrupted while validating Jira plugin installation: " + e.getMessage());
        }
    }

    private JiraServerProperties createJiraProperties(FieldModel fieldModel) {
        String url = fieldModel.getFieldValue(JiraServerDescriptor.KEY_SERVER_URL).orElse("");
        String username = fieldModel.getFieldValue(JiraServerDescriptor.KEY_SERVER_USERNAME).orElse("");
        String password = fieldModel.getFieldValueModel(JiraServerDescriptor.KEY_SERVER_PASSWORD)
                              .map(this::getAppropriateAccessToken)
                              .orElse("");

        return new JiraServerProperties(url, password, username);
    }

    private String getAppropriateAccessToken(FieldValueModel fieldAccessToken) {
        String accessToken = fieldAccessToken.getValue().orElse("");
        boolean accessTokenSet = fieldAccessToken.isSet();
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

    private boolean isJiraPluginInstalled(PluginManagerService jiraAppService, String accessToken, String username, String appKey) throws IntegrationException, InterruptedException {
        long maxTimeForChecks = 5L;
        long checkAgain = 1L;
        while (checkAgain <= maxTimeForChecks) {
            boolean foundPlugin = jiraAppService.isAppInstalled(username, accessToken, appKey);
            if (foundPlugin) {
                return true;
            }

            TimeUnit.SECONDS.sleep(checkAgain);
            checkAgain++;
        }

        return false;
    }

    private ActionResult<String> createBadRequestIntegrationException(IntegrationException error) {
        logger.error("There was an issue connecting to Jira server", error);
        return new ActionResult<>(HttpStatus.BAD_REQUEST, "The following error occurred when connecting to Jira server: " + error.getMessage());
    }

}
