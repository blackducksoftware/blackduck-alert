/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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

import java.io.IOException;
import java.util.Map;

import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.JiraChannel;
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
import com.synopsys.integration.jira.common.cloud.rest.JiraCloudHttpClient;
import com.synopsys.integration.jira.common.cloud.rest.service.JiraCloudServiceFactory;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.body.StringBodyContent;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;
import com.synopsys.integration.util.ResourceUtil;

@Component
public class JiraCustomEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(JiraCustomEndpoint.class);

    public static final String JIRA_PLUGIN_URL = "/rest/plugins/1.0/";

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
            final JiraCloudHttpClient httpClient = jiraServicesCloudFactory.getHttpClient();
            final String url = jiraProperties.getUrl();
            final String username = jiraProperties.getUsername();
            final String accessToken = jiraProperties.getAccessToken();
            final String pluginToken = retrievePluginToken(httpClient, url, username, accessToken);
            final Request request = uploadJiraPlugin(url, username, accessToken, pluginToken);
            final Response response = httpClient.execute(request);
            if (response.isStatusCodeError()) {
                return responseFactory.createBadRequestResponse("", "The Jira Cloud server responded with error code: " + response.getStatusCode());
            }
            return responseFactory.createOkResponse("", "Successfully created Alert plugin on Jira Cloud server.");
        } catch (final IntegrationException e) {
            logger.error("There was an issue connecting to Jira Cloud", e);
            return responseFactory.createBadRequestResponse("", "The following error occurred when connecting to Jira Cloud: " + e.getMessage());
        } catch (final IOException e) {
            logger.error("There was a problem reading the plugin file", e);
            return responseFactory.createInternalServerErrorResponse("", "There was a problem reading the plugin file: " + e.getMessage());
        }
    }

    private JiraProperties createJiraProperties(final Map<String, FieldValueModel> fieldValueModels) {
        final FieldValueModel fieldUrl = fieldValueModels.get(JiraDescriptor.KEY_JIRA_URL);
        final FieldValueModel fieldAccessToken = fieldValueModels.get(JiraDescriptor.KEY_JIRA_ACCESS_TOKEN);
        final FieldValueModel fieldUsername = fieldValueModels.get(JiraDescriptor.KEY_JIRA_USERNAME);

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
                           .flatMap(configurationModel -> configurationModel.getField(JiraDescriptor.KEY_JIRA_ACCESS_TOKEN))
                           .flatMap(ConfigurationFieldModel::getFieldValue)
                           .orElse("");

            } catch (final AlertDatabaseConstraintException e) {
                logger.error("Was unable to retrieve existing Jira configuration.");
            }
        }

        return accessToken;
    }

    private String retrievePluginToken(final JiraCloudHttpClient jiraHttpClient, final String url, final String username, final String accessToken) throws IntegrationException {
        final Request.Builder requestBuilder = createBasicRequestBuilder(url, username, accessToken);
        requestBuilder.addQueryParameter("os_authType", "basic");
        requestBuilder.method(HttpMethod.GET);
        requestBuilder.addAdditionalHeader("Accept", "application/vnd.atl.plugins.installed+json");
        final Response response = jiraHttpClient.execute(requestBuilder.build());
        return response.getHeaderValue("upm-token");
    }

    private Request uploadJiraPlugin(final String url, final String username, final String accessToken, final String pluginToken) throws IOException {
        final String pluginJson = ResourceUtil.getResourceAsString(JiraCustomEndpoint.class, "/jira/jiraIssueSearchPlugin.json", Charsets.UTF_8);
        final Request.Builder requestBuilder = createBasicRequestBuilder(url, username, accessToken);
        requestBuilder.addQueryParameter("token", pluginToken);
        requestBuilder.method(HttpMethod.POST);
        requestBuilder.addAdditionalHeader("Content-Type", "application/vnd.atl.plugins.install.uri+json");
        requestBuilder.addAdditionalHeader("Accept", "application/json");
        requestBuilder.bodyContent(new StringBodyContent(pluginJson));
        return requestBuilder.build();
    }

    private Request.Builder createBasicRequestBuilder(final String baseUrl, final String username, final String accessToken) {
        final Request.Builder requestBuilder = Request.newBuilder();

        requestBuilder.uri(baseUrl + JIRA_PLUGIN_URL);
        final byte[] authorizationBytes = String.format("%s:%s", username, accessToken).getBytes(Charsets.UTF_8);
        final String authorization = String.format("Basic %s", Base64.encodeBase64String(authorizationBytes));
        requestBuilder.addAdditionalHeader("authorization", authorization);
        return requestBuilder;
    }

}
