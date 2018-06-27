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
package com.blackducksoftware.integration.hub.alert.web.test.controller;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.descriptor.ChannelDescriptor;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.controller.handler.ControllerHandler;
import com.blackducksoftware.integration.hub.alert.web.model.ResponseBodyBuilder;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;
import com.blackducksoftware.integration.rest.exception.IntegrationRestException;

@Component
public class UniversalDistributionConfigHandler extends ControllerHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final UniversalDistributionConfigActions universalDistributionConfigActions;

    @Autowired
    public UniversalDistributionConfigHandler(final ObjectTransformer objectTransformer, final UniversalDistributionConfigActions universalDistributionConfigActions) {
        super(objectTransformer);
        this.universalDistributionConfigActions = universalDistributionConfigActions;
    }

    public List<CommonDistributionConfigRestModel> getConfig(final Long id, final ChannelDescriptor descriptor) {
        try {
            return universalDistributionConfigActions.getConfig(id, descriptor);
        } catch (final AlertException e) {
            logger.error(e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    public ResponseEntity<String> postConfig(final CommonDistributionConfigRestModel restModel, final ChannelDescriptor descriptor) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing " + descriptor.getDistributionRestModelClass().getSimpleName());
        }
        if (!universalDistributionConfigActions.getConfigActions().doesConfigExist(restModel.getId(), descriptor.getDistributionRepository())) {
            try {
                universalDistributionConfigActions.validateConfig(restModel, descriptor);
                try {
                    final DatabaseEntity updatedEntity = universalDistributionConfigActions.saveConfig(restModel, descriptor);
                    return createResponse(HttpStatus.CREATED, updatedEntity.getId(), "Created");
                } catch (final AlertException e) {
                    logger.error(e.getMessage(), e);
                    return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, restModel.getId(), e.getMessage());
                }
            } catch (final AlertFieldException e) {
                final ResponseBodyBuilder responseBuilder = new ResponseBodyBuilder(getObjectTransformer().stringToLong(restModel.getId()), "There were errors with the configuration.");
                responseBuilder.putErrors(e.getFieldErrors());
                return new ResponseEntity<>(responseBuilder.build(), HttpStatus.BAD_REQUEST);
            }
        }
        return createResponse(HttpStatus.CONFLICT, restModel.getId(), "Provided id must not be in use. To update an existing configuration, use PUT.");
    }

    public ResponseEntity<String> putConfig(final CommonDistributionConfigRestModel restModel, final ChannelDescriptor descriptor) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing " + descriptor.getDistributionRestModelClass().getSimpleName());
        }
        if (universalDistributionConfigActions.getConfigActions().doesConfigExist(restModel.getId(), descriptor.getDistributionRepository())) {
            try {
                universalDistributionConfigActions.validateConfig(restModel, descriptor);
                try {
                    final DatabaseEntity updatedEntity = universalDistributionConfigActions.saveNewConfigUpdateFromSavedConfig(restModel, descriptor);
                    return createResponse(HttpStatus.ACCEPTED, updatedEntity.getId(), "Updated");
                } catch (final AlertException e) {
                    logger.error(e.getMessage(), e);
                    return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, restModel.getId(), e.getMessage());
                }
            } catch (final AlertFieldException e) {
                final ResponseBodyBuilder responseBuilder = new ResponseBodyBuilder(getObjectTransformer().stringToLong(restModel.getId()), "There were errors with the configuration.");
                responseBuilder.putErrors(e.getFieldErrors());
                return new ResponseEntity<>(responseBuilder.build(), HttpStatus.BAD_REQUEST);
            }
        }
        return createResponse(HttpStatus.BAD_REQUEST, restModel.getId(), "No configuration with the specified id.");
    }

    public ResponseEntity<String> deleteConfig(final CommonDistributionConfigRestModel restModel, final ChannelDescriptor descriptor) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing " + descriptor.getDistributionRestModelClass().getSimpleName());
        }
        if (universalDistributionConfigActions.getConfigActions().doesConfigExist(restModel.getId(), descriptor.getDistributionRepository())) {
            universalDistributionConfigActions.deleteConfig(restModel.getId(), descriptor);
            return createResponse(HttpStatus.ACCEPTED, restModel.getId(), "Deleted");
        }
        return createResponse(HttpStatus.BAD_REQUEST, restModel.getId(), "No configuration with the specified id.");
    }

    public ResponseEntity<String> validateConfig(final CommonDistributionConfigRestModel restModel, final ChannelDescriptor descriptor) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing " + descriptor.getDistributionRestModelClass().getSimpleName());
        }
        try {
            final String responseMessage = universalDistributionConfigActions.validateConfig(restModel, descriptor);
            return createResponse(HttpStatus.OK, restModel.getId(), responseMessage);
        } catch (final AlertFieldException e) {
            final ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder(getObjectTransformer().stringToLong(restModel.getId()), e.getMessage());
            responseBodyBuilder.putErrors(e.getFieldErrors());
            final String responseBody = responseBodyBuilder.build();
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<String> testConfig(final CommonDistributionConfigRestModel restModel, final ChannelDescriptor descriptor) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing " + descriptor.getDistributionRestModelClass().getSimpleName());
        }
        try {
            final String responseMessage = universalDistributionConfigActions.testConfig(restModel, descriptor);
            return createResponse(HttpStatus.OK, restModel.getId(), responseMessage);
        } catch (final IntegrationRestException e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.valueOf(e.getHttpStatusCode()), restModel.getId(), e.getHttpStatusMessage() + " : " + e.getMessage());
        } catch (final AlertFieldException e) {
            final ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder(getObjectTransformer().stringToLong(restModel.getId()), e.getMessage());
            responseBodyBuilder.putErrors(e.getFieldErrors());
            final String responseBody = responseBodyBuilder.build();
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, restModel.getId(), e.getMessage());
        }
    }

}
