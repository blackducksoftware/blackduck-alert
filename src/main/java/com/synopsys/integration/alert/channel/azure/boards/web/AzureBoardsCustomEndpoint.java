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
package com.synopsys.integration.alert.channel.azure.boards.web;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.channel.azure.boards.service.AzureOAuthScopes;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.oauth.OAuthCustomEndpoint;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.oauth.OAuthEndpointResponse;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.azure.boards.common.http.AzureHttpServiceFactory;

@Component
public class AzureBoardsCustomEndpoint extends OAuthCustomEndpoint {
    private final Logger logger = LoggerFactory.getLogger(AzureBoardsCustomEndpoint.class);

    private final AlertProperties alertProperties;
    private final ConfigurationAccessor configurationAccessor;
    private final ConfigurationFieldModelConverter modelConverter;

    public AzureBoardsCustomEndpoint(CustomEndpointManager customEndpointManager, ResponseFactory responseFactory, Gson gson, AlertProperties alertProperties, ConfigurationAccessor configurationAccessor,
        ConfigurationFieldModelConverter modelConverter)
        throws AlertException {
        super(AzureBoardsDescriptor.KEY_OAUTH, customEndpointManager, responseFactory, gson);
        this.alertProperties = alertProperties;
        this.configurationAccessor = configurationAccessor;
        this.modelConverter = modelConverter;
    }

    @Override
    protected OAuthEndpointResponse createOAuthResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper) {
        try {
            FieldAccessor fieldAccessor = createFieldAccessor(fieldModel);
            Optional<String> clientId = fieldAccessor.getString(AzureBoardsDescriptor.KEY_CLIENT_ID);
            if (!clientId.isPresent()) {
                return new OAuthEndpointResponse(HttpStatus.BAD_REQUEST.value(), false, "", "client id not found.");
            }
            Optional<String> alertServerUrl = alertProperties.getServerUrl();

            if (!alertServerUrl.isPresent()) {
                return new OAuthEndpointResponse(HttpStatus.BAD_REQUEST.value(), false, "", "Could not determine the alert server url for the callback.");
            }
            String authUrl = createAuthURL(clientId.get(), alertServerUrl.get());
            logger.debug("Authenticating Azure OAuth URL: " + authUrl);
            //TODO add code to check if Alert has already been authorized to set the authenticated flag.

            return new OAuthEndpointResponse(HttpStatus.OK.value(), false, authUrl, "");

        } catch (Exception ex) {
            logger.error("Error activating Azure Boards", ex);
            return new OAuthEndpointResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), false, "", "Error activating azure oauth.");
        }
    }

    private FieldAccessor createFieldAccessor(FieldModel fieldModel) {
        Map<String, ConfigurationFieldModel> fields = new HashMap<>();
        try {
            fields.putAll(modelConverter.convertToConfigurationFieldModelMap(fieldModel));
            // check if a configuration exists because the client id is a sensitive field and won't have a value in the field model if updating.
            if (StringUtils.isNotBlank(fieldModel.getId())) {
                configurationAccessor.getConfigurationById(Long.valueOf(fieldModel.getId()))
                    .map(ConfigurationModel::getCopyOfKeyToFieldMap)
                    .ifPresent(fields::putAll);
            }
        } catch (AlertDatabaseConstraintException ex) {
            logger.error("Error creating field accessor for Azure authentication", ex);
        }
        return new FieldAccessor(fields);
    }

    private String createAuthURL(String clientId, String alertServerUrl) {
        StringBuilder authUrlBuilder = new StringBuilder(300);
        authUrlBuilder.append(AzureHttpServiceFactory.DEFAULT_AUTHORIZATION_URL);
        authUrlBuilder.append(createQueryString(clientId, alertServerUrl));
        return authUrlBuilder.toString();
    }

    private String createQueryString(String clientId, String alertServerUrl) {
        String authorizationUrl = String.format("%s%s", alertServerUrl, AzureOauthCallbackController.AZURE_OAUTH_CALLBACK_PATH);
        StringBuilder queryBuilder = new StringBuilder(250);
        queryBuilder.append("&client_id=");
        queryBuilder.append(clientId);
        //TODO have an object that stores the request keys and purges them after some amount of time.
        queryBuilder.append("&state=");
        queryBuilder.append(createRequestKey());
        queryBuilder.append("&scope=vso.project_write%20vso.work_full");
        queryBuilder.append(AzureOAuthScopes.PROJECTS_READ.getScope());
        queryBuilder.append("%20");
        queryBuilder.append(AzureOAuthScopes.WORK_FULL.getScope());
        queryBuilder.append("&redirect_uri=");
        queryBuilder.append(URLEncoder.encode(authorizationUrl, StandardCharsets.UTF_8));
        return queryBuilder.toString();
    }

    private String createRequestKey() {
        UUID requestID = UUID.randomUUID();
        return String.format("%s-%s", "alert-auth-request", requestID.toString());
    }
}
