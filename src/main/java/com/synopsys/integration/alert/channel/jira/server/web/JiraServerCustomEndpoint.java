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

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.cloud.web.JiraCustomEndpoint;
import com.synopsys.integration.alert.channel.jira.server.JiraServerChannelKey;
import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.ButtonCustomEndpoint;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.issuetracker.jira.common.JiraConstants;
import com.synopsys.integration.issuetracker.jira.server.JiraServerProperties;
import com.synopsys.integration.jira.common.rest.service.PluginManagerService;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;
import com.synopsys.integration.rest.request.Response;

@Component
public class JiraServerCustomEndpoint extends ButtonCustomEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(JiraCustomEndpoint.class);

    private final JiraServerChannelKey jiraChannelKey;
    private final ResponseFactory responseFactory;
    private final ConfigurationAccessor configurationAccessor;
    private final Gson gson;

    @Autowired
    public JiraServerCustomEndpoint(JiraServerChannelKey jiraChannelKey, CustomEndpointManager customEndpointManager, ResponseFactory responseFactory, ConfigurationAccessor configurationAccessor, Gson gson) throws AlertException {
        super(JiraServerDescriptor.KEY_JIRA_SERVER_CONFIGURE_PLUGIN, customEndpointManager, responseFactory);
        this.jiraChannelKey = jiraChannelKey;
        this.responseFactory = responseFactory;
        this.configurationAccessor = configurationAccessor;
        this.gson = gson;
    }

    @Override
    public Optional<ResponseEntity<String>> preprocessRequest(FieldModel fieldModel) {
        JiraServerProperties jiraProperties = createJiraProperties(fieldModel);
        try {
            JiraServerServiceFactory jiraServicesFactory = jiraProperties.createJiraServicesServerFactory(logger, gson);
            PluginManagerService jiraAppService = jiraServicesFactory.createPluginManagerService();
            String username = jiraProperties.getUsername();
            String password = jiraProperties.getPassword();
            Response response = jiraAppService.installMarketplaceServerApp(JiraConstants.JIRA_APP_KEY, username, password);
            if (response.isStatusCodeError()) {
                return Optional.of(responseFactory.createBadRequestResponse("", "The Jira server responded with error code: " + response.getStatusCode()));
            }
            boolean jiraPluginInstalled = isJiraPluginInstalled(jiraAppService, password, username, JiraConstants.JIRA_APP_KEY);
            if (!jiraPluginInstalled) {
                return Optional.of(responseFactory.createNotFoundResponse("Was not able to confirm Jira server successfully installed the Jira Server plugin. Please verify the installation on you Jira server."));
            }
        } catch (IntegrationException e) {
            logger.error("There was an issue connecting to Jira server", e);
            return Optional.of(responseFactory.createBadRequestResponse("", "The following error occurred when connecting to Jira server: " + e.getMessage()));
        } catch (InterruptedException e) {
            logger.error("Thread was interrupted while validating jira install.", e);
            Thread.currentThread().interrupt();
            return Optional.of(responseFactory.createInternalServerErrorResponse("", "Thread was interrupted while validating Jira plugin installation: " + e.getMessage()));
        }

        return Optional.empty();
    }

    @Override
    protected String createData(FieldModel fieldModel) throws AlertException {
        return "Successfully created Alert plugin on Jira server.";
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
                return configurationAccessor.getConfigurationByDescriptorKeyAndContext(jiraChannelKey, ConfigContextEnum.GLOBAL)
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
            boolean foundPlugin = jiraAppService.getInstalledApp(username, accessToken, appKey).isPresent();
            if (foundPlugin) {
                return true;
            }

            TimeUnit.SECONDS.sleep(checkAgain);
            checkAgain++;
        }

        return false;
    }
}
