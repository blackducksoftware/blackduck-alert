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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.ButtonCustomEndpoint;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.azure.boards.common.http.AzureHttpServiceFactory;

@Component
public class AzureBoardsCustomEndpoint extends ButtonCustomEndpoint {
    private final Logger logger = LoggerFactory.getLogger(AzureBoardsCustomEndpoint.class);

    private final ResponseFactory responseFactory;
    private final AlertProperties alertProperties;

    public AzureBoardsCustomEndpoint(CustomEndpointManager customEndpointManager, ResponseFactory responseFactory, AlertProperties alertProperties) throws AlertException {
        super(AzureBoardsDescriptor.KEY_OAUTH, customEndpointManager);
        this.responseFactory = responseFactory;
        this.alertProperties = alertProperties;
    }

    @Override
    public ResponseEntity<String> createResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper) {
        try {
            Optional<String> clientId = fieldModel.getFieldValue(AzureBoardsDescriptor.KEY_CLIENT_ID);
            if (!clientId.isPresent()) {
                return responseFactory.createBadRequestResponse("", "client id not found.");
            }
            Optional<String> alertServerUrl = alertProperties.getServerUrl();

            if (!alertServerUrl.isPresent()) {
                return responseFactory.createBadRequestResponse("", "Could not determine the alert server url for the callback.");
            }

            StringBuilder authUrlBuilder = new StringBuilder(300);
            authUrlBuilder.append(AzureHttpServiceFactory.DEFAULT_AUTHORIZATION_URL);
            authUrlBuilder.append("&client_id=");
            authUrlBuilder.append(clientId.get());
            authUrlBuilder.append("&state=alertAuth");
            authUrlBuilder.append("&scope=vso.work%20vso.code_write");
            authUrlBuilder.append("&redirect_uri=");
            authUrlBuilder.append(alertServerUrl.get());
            authUrlBuilder.append(AzureOauthCallbackController.AZURE_OAUTH_CALLBACK_PATH);
            String authUrl = authUrlBuilder.toString();
            logger.info("Azure OAuth URL: " + authUrl);
            return responseFactory.createOkResponse("", "Success"); //FIXME this message should fixed once the Oauth is implemented.

        } catch (Exception ex) {
            logger.error("Error activating Azure Boards", ex);
            return responseFactory.createInternalServerErrorResponse("", "Error activating azure oauth.");
        }
    }
}
