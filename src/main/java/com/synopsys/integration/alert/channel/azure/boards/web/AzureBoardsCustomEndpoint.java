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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.ButtonCustomEndpoint;
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
public class AzureBoardsCustomEndpoint extends ButtonCustomEndpoint {
    private final Logger logger = LoggerFactory.getLogger(AzureBoardsCustomEndpoint.class);

    private final ResponseFactory responseFactory;
    private final AlertProperties alertProperties;
    private final ConfigurationAccessor configurationAccessor;
    private final ConfigurationFieldModelConverter modelConverter;

    public AzureBoardsCustomEndpoint(CustomEndpointManager customEndpointManager, ResponseFactory responseFactory, AlertProperties alertProperties, ConfigurationAccessor configurationAccessor,
        ConfigurationFieldModelConverter modelConverter)
        throws AlertException {
        super(AzureBoardsDescriptor.KEY_OAUTH, customEndpointManager);
        this.responseFactory = responseFactory;
        this.alertProperties = alertProperties;
        this.configurationAccessor = configurationAccessor;
        this.modelConverter = modelConverter;
    }

    @Override
    public ResponseEntity<String> createResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper) {
        try {
            FieldAccessor fieldAccessor = createFieldAccessor(fieldModel);
            Optional<String> clientId = fieldAccessor.getString(AzureBoardsDescriptor.KEY_CLIENT_ID);
            if (!clientId.isPresent()) {
                return responseFactory.createBadRequestResponse("", "client id not found.");
            }
            Optional<String> alertServerUrl = alertProperties.getServerUrl();

            if (!alertServerUrl.isPresent()) {
                return responseFactory.createBadRequestResponse("", "Could not determine the alert server url for the callback.");
            }
            String authUrl = createAuthURL(clientId.get(), alertServerUrl.get());
            logger.info("Authenticating Azure OAuth URL: " + authUrl);
            return responseFactory.createFoundRedirectResponse(authUrl);

        } catch (Exception ex) {
            logger.error("Error activating Azure Boards", ex);
            return responseFactory.createInternalServerErrorResponse("", "Error activating azure oauth.");
        }
    }

    private FieldAccessor createFieldAccessor(FieldModel fieldModel) {
        Map<String, ConfigurationFieldModel> fields = new HashMap<>();
        try {
            fields.putAll(modelConverter.convertToConfigurationFieldModelMap(fieldModel));
            // check if a configuration exists because the client id is a sensitive field and won't have a value in the field model if updating.
            if (StringUtils.isNotBlank(fieldModel.getId())) {
                Optional<ConfigurationModel> configurationFieldModel = configurationAccessor.getConfigurationById(Long.valueOf(fieldModel.getId()));
                configurationFieldModel.ifPresent(model -> fields.putAll(model.getCopyOfKeyToFieldMap()));
            }
        } catch (AlertDatabaseConstraintException ex) {
            logger.error("Error creating field acessor for Azure authentication", ex);
        }
        return new FieldAccessor(fields);
    }

    private String createAuthURL(String clientId, String alertServerUrl) {
        StringBuilder authUrlBuilder = new StringBuilder(300);
        authUrlBuilder.append(AzureHttpServiceFactory.DEFAULT_AUTHORIZATION_URL);
        authUrlBuilder.append("&client_id=");
        authUrlBuilder.append(clientId);
        //TODO have an object that stores the request keys and purges them after some amount of time.
        authUrlBuilder.append("&state=");
        authUrlBuilder.append(createRequestKey());
        authUrlBuilder.append("&scope=vso.work%20vso.code_write");
        authUrlBuilder.append("&redirect_uri=");
        authUrlBuilder.append(alertServerUrl);
        authUrlBuilder.append(AzureOauthCallbackController.AZURE_OAUTH_CALLBACK_PATH);
        return authUrlBuilder.toString();
    }

    private String createRequestKey() {
        UUID requestID = UUID.randomUUID();
        return String.format("%s-%s", "alert-auth-request", requestID.toString());
    }
}
