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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.ConfigActions;
import com.blackducksoftware.integration.hub.alert.web.controller.handler.ControllerHandler;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

public class UniversalControllerHandler<R extends ConfigRestModel> extends ControllerHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ConfigActions configActions;

    public UniversalControllerHandler(final ObjectTransformer objectTransformer, final ConfigActions configActions) {
        super(objectTransformer);
        this.configActions = configActions;
    }
    //
    // public List<R> getConfig(final Long id, final Class<R> restModelClass, final JpaRepository<DatabaseEntity, Long> repository) {
    // try {
    // return configActions.getConfig(id, restModelClass, repository);
    // } catch (final AlertException e) {
    // logger.error(e.getMessage(), e);
    // }
    // return Collections.emptyList();
    // }
    //
    // public ResponseEntity<String> postConfig(final R restModel, final Class<R> restModelClass, final Class<? extends DatabaseEntity> entityClass, final JpaRepository<DatabaseEntity, Long> repository,
    // final SimpleConfigActions<R> simpleConfigActions) {
    // if (restModel == null) {
    // return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing " + restModelClass.getSimpleName());
    // }
    // if (!configActions.doesConfigExist(restModel.getId(), repository)) {
    // try {
    // configActions.validateConfig(restModel, simpleConfigActions);
    // try {
    // final DatabaseEntity updatedEntity = configActions.saveConfig(restModel, repository, entityClass);
    // return createResponse(HttpStatus.CREATED, updatedEntity.getId(), "Created");
    // } catch (final AlertException e) {
    // logger.error(e.getMessage(), e);
    // return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, restModel.getId(), e.getMessage());
    // }
    // } catch (final AlertFieldException e) {
    // final ResponseBodyBuilder responseBuilder = new ResponseBodyBuilder(getObjectTransformer().stringToLong(restModel.getId()), "There were errors with the configuration.");
    // responseBuilder.putErrors(e.getFieldErrors());
    // return new ResponseEntity<>(responseBuilder.build(), HttpStatus.BAD_REQUEST);
    // }
    // }
    // return createResponse(HttpStatus.CONFLICT, restModel.getId(), "Provided id must not be in use. To update an existing configuration, use PUT.");
    // }
    //
    // public ResponseEntity<String> putConfig(final R restModel, final Class<R> restModelClass, final Class<? extends DatabaseEntity> entityClass, final JpaRepository<DatabaseEntity, Long> repository,
    // final SimpleConfigActions<R> simpleConfigActions) {
    // if (restModel == null) {
    // return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing " + restModelClass.getSimpleName());
    // }
    // if (!configActions.doesConfigExist(restModel.getId(), repository)) {
    // try {
    // configActions.validateConfig(restModel, simpleConfigActions);
    // try {
    // final DatabaseEntity updatedEntity = configActions.saveNewConfigUpdateFromSavedConfig(restModel, repository, entityClass);
    // return createResponse(HttpStatus.ACCEPTED, updatedEntity.getId(), "Updated");
    // } catch (final AlertException e) {
    // logger.error(e.getMessage(), e);
    // return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, restModel.getId(), e.getMessage());
    // }
    // } catch (final AlertFieldException e) {
    // final ResponseBodyBuilder responseBuilder = new ResponseBodyBuilder(getObjectTransformer().stringToLong(restModel.getId()), "There were errors with the configuration.");
    // responseBuilder.putErrors(e.getFieldErrors());
    // return new ResponseEntity<>(responseBuilder.build(), HttpStatus.BAD_REQUEST);
    // }
    // }
    // return createResponse(HttpStatus.BAD_REQUEST, restModel.getId(), "No configuration with the specified id.");
    // }
    //
    // public ResponseEntity<String> deleteConfig(final R restModel, final Class<R> restModelClass, final JpaRepository<DatabaseEntity, Long> repository) {
    // if (restModel == null) {
    // return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing " + restModelClass.getSimpleName());
    // }
    // if (configActions.doesConfigExist(restModel.getId(), repository)) {
    // configActions.deleteConfig(restModel.getId(), repository);
    // return createResponse(HttpStatus.ACCEPTED, restModel.getId(), "Deleted");
    // }
    // return createResponse(HttpStatus.BAD_REQUEST, restModel.getId(), "No configuration with the specified id.");
    // }
    //
    // public ResponseEntity<String> validateConfig(final R restModel, final Class<R> restModelClass, final SimpleConfigActions<R> simpleConfigActions) {
    // if (restModel == null) {
    // return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing " + restModelClass.getSimpleName());
    // }
    // try {
    // final String responseMessage = configActions.validateConfig(restModel, simpleConfigActions);
    // return createResponse(HttpStatus.OK, restModel.getId(), responseMessage);
    // } catch (final AlertFieldException e) {
    // final ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder(getObjectTransformer().stringToLong(restModel.getId()), e.getMessage());
    // responseBodyBuilder.putErrors(e.getFieldErrors());
    // final String responseBody = responseBodyBuilder.build();
    // return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    // }
    // }
    //
    // public ResponseEntity<String> testConfig(final R restModel, final Class<R> restModelClass, final String channelName) {
    // if (restModel == null) {
    // return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing " + restModelClass.getSimpleName());
    // }
    // try {
    // final String responseMessage = configActions.testConfig(restModel, channelName);
    // return createResponse(HttpStatus.OK, restModel.getId(), responseMessage);
    // } catch (final IntegrationRestException e) {
    // logger.error(e.getMessage(), e);
    // return createResponse(HttpStatus.valueOf(e.getHttpStatusCode()), restModel.getId(), e.getHttpStatusMessage() + " : " + e.getMessage());
    // } catch (final AlertFieldException e) {
    // final ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder(getObjectTransformer().stringToLong(restModel.getId()), e.getMessage());
    // responseBodyBuilder.putErrors(e.getFieldErrors());
    // final String responseBody = responseBodyBuilder.build();
    // return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    // } catch (final Exception e) {
    // logger.error(e.getMessage(), e);
    // return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, restModel.getId(), e.getMessage());
    // }
    // }
}
