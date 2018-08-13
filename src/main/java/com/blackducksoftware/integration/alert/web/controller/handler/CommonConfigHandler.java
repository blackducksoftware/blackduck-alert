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
package com.blackducksoftware.integration.alert.web.controller.handler;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.exception.AlertException;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.web.actions.OldConfigActions;
import com.blackducksoftware.integration.alert.web.exception.AlertFieldException;
import com.blackducksoftware.integration.alert.web.model.Config;
import com.blackducksoftware.integration.alert.web.model.ResponseBodyBuilder;
import com.blackducksoftware.integration.rest.exception.IntegrationRestException;

public class CommonConfigHandler<D extends DatabaseEntity, R extends Config, W extends JpaRepository<D, Long>> extends ControllerHandler {
    private final Logger logger = LoggerFactory.getLogger(CommonConfigHandler.class);
    public final Class<D> databaseEntityClass;
    public final Class<R> configRestModelClass;
    public final OldConfigActions<D, R, W> oldConfigActions;

    public CommonConfigHandler(final Class<D> databaseEntityClass, final Class<R> configRestModelClass, final OldConfigActions<D, R, W> configActions, final ContentConverter contentConverter) {
        super(contentConverter);
        this.databaseEntityClass = databaseEntityClass;
        this.configRestModelClass = configRestModelClass;
        this.oldConfigActions = configActions;
    }

    public List<R> getConfig(final Long id) {
        try {
            return oldConfigActions.getConfig(id);
        } catch (final AlertException e) {
            logger.error(e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    public ResponseEntity<String> postConfig(final R restModel) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing " + configRestModelClass.getSimpleName());
        }
        if (!oldConfigActions.doesConfigExist(restModel.getId())) {
            try {
                oldConfigActions.validateConfig(restModel);
                oldConfigActions.configurationChangeTriggers(restModel);
                try {
                    final D updatedEntity = oldConfigActions.saveConfig(restModel);
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

    public ResponseEntity<String> putConfig(final R restModel) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing " + configRestModelClass.getSimpleName());
        }
        if (oldConfigActions.doesConfigExist(restModel.getId())) {
            try {
                oldConfigActions.validateConfig(restModel);
                oldConfigActions.configurationChangeTriggers(restModel);
                try {
                    final D updatedEntity = oldConfigActions.saveNewConfigUpdateFromSavedConfig(restModel);
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

    public ResponseEntity<String> deleteConfig(final R restModel) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing " + configRestModelClass.getSimpleName());
        }
        if (oldConfigActions.doesConfigExist(restModel.getId())) {
            oldConfigActions.deleteConfig(restModel.getId());
            return createResponse(HttpStatus.ACCEPTED, restModel.getId(), "Deleted");
        }
        return createResponse(HttpStatus.BAD_REQUEST, restModel.getId(), "No configuration with the specified id.");
    }

    public ResponseEntity<String> validateConfig(final R restModel) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing " + configRestModelClass.getSimpleName());
        }
        try {
            final String responseMessage = oldConfigActions.validateConfig(restModel);
            return createResponse(HttpStatus.OK, restModel.getId(), responseMessage);
        } catch (final AlertFieldException e) {
            final ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder(getContentConverter().getLongValue(restModel.getId()), e.getMessage());
            responseBodyBuilder.putErrors(e.getFieldErrors());
            final String responseBody = responseBodyBuilder.build();
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<String> testConfig(final R restModel) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing " + configRestModelClass.getSimpleName());
        }
        try {
            final String responseMessage = "";
            oldConfigActions.testConfig(restModel);
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
