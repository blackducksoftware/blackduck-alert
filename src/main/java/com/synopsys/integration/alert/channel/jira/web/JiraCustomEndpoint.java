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
package com.synopsys.integration.alert.channel.jira.web;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.JiraChannel;
import com.synopsys.integration.alert.channel.jira.JiraConstants;
import com.synopsys.integration.alert.channel.jira.JiraProperties;
import com.synopsys.integration.alert.channel.jira.descriptor.JiraDescriptor;
import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.web.controller.ResponseFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.rest.service.JiraAppService;
import com.synopsys.integration.jira.common.cloud.rest.service.JiraCloudServiceFactory;
import com.synopsys.integration.rest.request.Response;

@Component
public class JiraCustomEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(JiraCustomEndpoint.class);

    private final ResponseFactory responseFactory;
    private final ConfigurationAccessor configurationAccessor;
    private final Gson gson;

    @Autowired
    public JiraCustomEndpoint(final CustomEndpointManager customEndpointManager, final ResponseFactory responseFactory, final ConfigurationAccessor configurationAccessor, final Gson gson) throws AlertException {
        this.responseFactory = responseFactory;
        this.configurationAccessor = configurationAccessor;
        this.gson = gson;

        customEndpointManager.registerFunction(JiraDescriptor.KEY_JIRA_CONFIGURE_PLUGIN, this::installJiraPlugin);
    }

    public ResponseEntity<String> installJiraPlugin(final Map<String, FieldValueModel> fieldValueModels) {
        final JiraProperties jiraProperties = createJiraProperties(fieldValueModels);
        try {
            final JiraCloudServiceFactory jiraServicesCloudFactory = jiraProperties.createJiraServicesCloudFactory(logger, gson);
            final JiraAppService jiraAppService = jiraServicesCloudFactory.createJiraAppService();
            final String username = jiraProperties.getUsername();
            final String accessToken = jiraProperties.getAccessToken();
            final Response response = jiraAppService.installMarketplaceApp(JiraConstants.JIRA_APP_KEY, username, accessToken);
            if (response.isStatusCodeError()) {
                return responseFactory.createBadRequestResponse("", "The Jira Cloud server responded with error code: " + response.getStatusCode());
            }
            final boolean jiraPluginInstalled = isJiraPluginInstalled(jiraAppService, accessToken, username, JiraConstants.JIRA_APP_KEY);
            if (!jiraPluginInstalled) {
                return responseFactory.createNotFoundResponse("Was not able to confirm Jira Cloud successfully installed the Jira Cloud plugin. Please verify the installation on you Jira Cloud server.");
            }
            return responseFactory.createOkResponse("", "Successfully created Alert plugin on Jira Cloud server.");
        } catch (final IntegrationException e) {
            logger.error("There was an issue connecting to Jira Cloud", e);
            return responseFactory.createBadRequestResponse("", "The following error occurred when connecting to Jira Cloud: " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("Thread was interrupted while validating jira install.", e);
            Thread.currentThread().interrupt();
            return responseFactory.createInternalServerErrorResponse("", "Thread was interrupted while validating Jira plugin installation: " + e.getMessage());
        }
    }

    private JiraProperties createJiraProperties(final Map<String, FieldValueModel> fieldValueModels) {
        final FieldValueModel fieldUrl = fieldValueModels.get(JiraDescriptor.KEY_JIRA_URL);
        final FieldValueModel fieldAccessToken = fieldValueModels.get(JiraDescriptor.KEY_JIRA_ADMIN_API_TOKEN);
        final FieldValueModel fieldUsername = fieldValueModels.get(JiraDescriptor.KEY_JIRA_ADMIN_EMAIL_ADDRESS);

        final String url = fieldUrl.getValue().orElse("");
        final String username = fieldUsername.getValue().orElse("");
        final String accessToken = getAppropriateAccessToken(fieldAccessToken);

        return new JiraProperties(url, accessToken, username);
    }

    private String getAppropriateAccessToken(final FieldValueModel fieldAccessToken) {
        final String accessToken = fieldAccessToken.getValue().orElse("");
        final boolean accessTokenSet = fieldAccessToken.isSet();
        if (StringUtils.isBlank(accessToken) && accessTokenSet) {
            try {
                return configurationAccessor.getConfigurationByDescriptorNameAndContext(JiraChannel.COMPONENT_NAME, ConfigContextEnum.GLOBAL)
                           .stream()
                           .findFirst()
                           .flatMap(configurationModel -> configurationModel.getField(JiraDescriptor.KEY_JIRA_ADMIN_API_TOKEN))
                           .flatMap(ConfigurationFieldModel::getFieldValue)
                           .orElse("");

            } catch (final AlertDatabaseConstraintException e) {
                logger.error("Unable to retrieve existing Jira configuration.");
            }
        }

        return accessToken;
    }

    private boolean isJiraPluginInstalled(JiraAppService jiraAppService, String accessToken, String username, String appKey) throws IntegrationException, InterruptedException {
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
