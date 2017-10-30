/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.web.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.web.actions.ConfigActions;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.ResponseBodyBuilder;

public class CommonConfigController<D extends DatabaseEntity, R extends ConfigRestModel> {
    private final Logger logger = LoggerFactory.getLogger(CommonConfigController.class);
    public final Class<D> databaseEntityClass;
    public final Class<R> configRestModelClass;
    public final ConfigActions<D, R> configActions;

    public CommonConfigController(final Class<D> databaseEntityClass, final Class<R> configRestModelClass, final ConfigActions<D, R> configActions) {
        this.databaseEntityClass = databaseEntityClass;
        this.configRestModelClass = configRestModelClass;
        this.configActions = configActions;
    }

    public List<R> getConfig(final Long id) {
        try {
            return configActions.getConfig(id);
        } catch (final IntegrationException e) {
            logger.error(e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    public ResponseEntity<String> postConfig(final R restModel) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing " + configRestModelClass.getSimpleName());
        }
        if (!configActions.doesConfigExist(restModel.getId())) {
            final Map<String, String> validationResults = configActions.validateConfig(restModel);
            if (validationResults.isEmpty()) {
                configActions.configurationChangeTriggers(restModel);
                try {
                    final D updatedEntity = configActions.saveConfig(restModel);
                    return createResponse(HttpStatus.CREATED, updatedEntity.getId(), "Created");
                } catch (final IntegrationException e) {
                    logger.error(e.getMessage(), e);
                    return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, restModel.getId(), e.getMessage());
                }
            } else {
                final ResponseBodyBuilder responseBuilder = new ResponseBodyBuilder(configActions.objectTransformer.stringToLong(restModel.getId()), "There were errors with the configuration.");
                responseBuilder.putErrors(validationResults);
                return new ResponseEntity<>(responseBuilder.build(), HttpStatus.BAD_REQUEST);
            }
        }
        return createResponse(HttpStatus.CONFLICT, restModel.getId(), "Provided id must not be in use. To update an existing configuration, use PUT.");
    }

    public ResponseEntity<String> putConfig(final R restModel) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing " + configRestModelClass.getSimpleName());
        }
        if (configActions.doesConfigExist(restModel.getId())) {
            final Map<String, String> validationResults = configActions.validateConfig(restModel);
            if (validationResults.isEmpty()) {
                configActions.configurationChangeTriggers(restModel);
                try {
                    final D updatedEntity = configActions.saveConfig(restModel);
                    return createResponse(HttpStatus.CREATED, updatedEntity.getId(), "Updated");
                } catch (final IntegrationException e) {
                    logger.error(e.getMessage(), e);
                    return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, restModel.getId(), e.getMessage());
                }
            } else {
                final ResponseBodyBuilder responseBuilder = new ResponseBodyBuilder(configActions.objectTransformer.stringToLong(restModel.getId()), "There were errors with the configuration.");
                responseBuilder.putErrors(validationResults);
                return new ResponseEntity<>(responseBuilder.build(), HttpStatus.BAD_REQUEST);
            }
        }
        return createResponse(HttpStatus.BAD_REQUEST, restModel.getId(), "No configuration with the specified id.");
    }

    public ResponseEntity<String> deleteConfig(final R restModel) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing " + configRestModelClass.getSimpleName());
        }
        if (configActions.doesConfigExist(restModel.getId())) {
            configActions.deleteConfig(restModel.getId());
            return createResponse(HttpStatus.ACCEPTED, restModel.getId(), "Deleted");
        }
        return createResponse(HttpStatus.BAD_REQUEST, restModel.getId(), "No configuration with the specified id.");
    }

    protected ResponseEntity<String> createResponse(final HttpStatus status, final String id, final String message) {
        return createResponse(status, configActions.objectTransformer.stringToLong(id), message);
    }

    protected ResponseEntity<String> createResponse(final HttpStatus status, final Long id, final String message) {
        final String responseBody = new ResponseBodyBuilder(id, message).build();
        return new ResponseEntity<>(responseBody, status);
    }

}
