/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.web.channel.handler;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.blackducksoftware.integration.alert.web.channel.actions.NewConfigActions;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.config.DescriptorConfig;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.controller.handler.ControllerHandler;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.alert.web.model.ResponseBodyBuilder;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class ChannelConfigHandler extends ControllerHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final NewConfigActions newConfigActions;

    public ChannelConfigHandler(final ContentConverter contentConverter, final NewConfigActions newConfigActions) {
        super(contentConverter);
        this.newConfigActions = newConfigActions;
    }

    public List<? extends Config> getConfig(final Long id, final DescriptorConfig descriptor) {
        try {
            return newConfigActions.getConfig(id, descriptor);
        } catch (final AlertException e) {
            logger.error(e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    public ResponseEntity<String> postConfig(final Config restModel, final DescriptorConfig descriptor) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing");
        }
        if (!newConfigActions.doesConfigExist(restModel.getId(), descriptor)) {
            try {
                newConfigActions.validateConfig(restModel, descriptor);
                try {
                    final DatabaseEntity updatedEntity = newConfigActions.saveConfig(restModel, descriptor);
                    return createResponse(HttpStatus.CREATED, updatedEntity.getId(), "Created");
                } catch (final AlertException e) {
                    logger.error(e.getMessage(), e);
                    return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, restModel.getId(), e.getMessage());
                }
            } catch (final AlertFieldException e) {
                final ResponseBodyBuilder responseBuilder = new ResponseBodyBuilder(getContentConverter().getLongValue(restModel.getId()), "There were errors with the configuration.");
                responseBuilder.putErrors(e.getFieldErrors());
                return new ResponseEntity<>(responseBuilder.build(), HttpStatus.BAD_REQUEST);
            }
        }
        return createResponse(HttpStatus.CONFLICT, restModel.getId(), "Provided id must not be in use. To update an existing configuration, use PUT.");
    }

    public ResponseEntity<String> putConfig(final Config restModel, final DescriptorConfig descriptor) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing");
        }
        if (newConfigActions.doesConfigExist(restModel.getId(), descriptor)) {
            try {
                newConfigActions.validateConfig(restModel, descriptor);
                try {
                    final DatabaseEntity updatedEntity = newConfigActions.saveNewConfigUpdateFromSavedConfig(restModel, descriptor);
                    return createResponse(HttpStatus.ACCEPTED, updatedEntity.getId(), "Updated");
                } catch (final AlertException e) {
                    logger.error(e.getMessage(), e);
                    return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, restModel.getId(), e.getMessage());
                }
            } catch (final AlertFieldException e) {
                final ResponseBodyBuilder responseBuilder = new ResponseBodyBuilder(getContentConverter().getLongValue(restModel.getId()), "There were errors with the configuration.");
                responseBuilder.putErrors(e.getFieldErrors());
                return new ResponseEntity<>(responseBuilder.build(), HttpStatus.BAD_REQUEST);
            }
        }
        return createResponse(HttpStatus.BAD_REQUEST, restModel.getId(), "No configuration with the specified id.");
    }

    public ResponseEntity<String> deleteConfig(final Long id, final DescriptorConfig descriptor) {
        if (id != null && newConfigActions.doesConfigExist(id, descriptor)) {
            newConfigActions.deleteConfig(id, descriptor);
            return createResponse(HttpStatus.ACCEPTED, id, "Deleted");
        }
        return createResponse(HttpStatus.BAD_REQUEST, id, "No configuration with the specified id.");
    }

    public ResponseEntity<String> validateConfig(final Config restModel, final DescriptorConfig descriptor) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing");
        }
        try {
            final String responseMessage = newConfigActions.validateConfig(restModel, descriptor);
            return createResponse(HttpStatus.OK, restModel.getId(), responseMessage);
        } catch (final AlertFieldException e) {
            final ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder(getContentConverter().getLongValue(restModel.getId()), e.getMessage());
            responseBodyBuilder.putErrors(e.getFieldErrors());
            final String responseBody = responseBodyBuilder.build();
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<String> testConfig(final Config restModel, final DescriptorConfig descriptor) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing");
        }
        try {
            final String responseMessage = newConfigActions.testConfig(restModel, descriptor);
            return createResponse(HttpStatus.OK, restModel.getId(), responseMessage);
        } catch (final IntegrationRestException e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.valueOf(e.getHttpStatusCode()), restModel.getId(), e.getHttpStatusMessage() + " : " + e.getMessage());
        } catch (final AlertFieldException e) {
            final ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder(getContentConverter().getLongValue(restModel.getId()), e.getMessage());
            responseBodyBuilder.putErrors(e.getFieldErrors());
            final String responseBody = responseBodyBuilder.build();
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, restModel.getId(), e.getMessage());
        }
    }
}
