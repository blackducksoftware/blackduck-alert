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
package com.synopsys.integration.alert.web.config;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.exception.AlertMethodNotAllowedException;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.web.controller.BaseController;
import com.synopsys.integration.alert.web.controller.ResponseFactory;
import com.synopsys.integration.rest.exception.IntegrationRestException;

@RestController
@RequestMapping(ConfigController.CONFIGURATION_PATH)
public class ConfigController extends BaseController {
    public static final String CONFIGURATION_PATH = BaseController.BASE_PATH + "/configuration";
    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);

    private final ConfigActions configActions;
    private final ContentConverter contentConverter;
    private final ResponseFactory responseFactory;
    private final AuthorizationManager authorizationManager;

    @Autowired
    public ConfigController(final ConfigActions configActions, final ContentConverter contentConverter, final ResponseFactory responseFactory, final AuthorizationManager authorizationManager) {
        this.configActions = configActions;
        this.contentConverter = contentConverter;
        this.responseFactory = responseFactory;
        this.authorizationManager = authorizationManager;
    }

    @GetMapping
    public ResponseEntity<String> getConfigs(final @RequestParam ConfigContextEnum context, @RequestParam(required = false) final String descriptorName) {
        final List<FieldModel> models;
        if (!authorizationManager.hasReadPermission(authorizationManager.generatePermissionKey(context.name(), descriptorName))) {
            return responseFactory.createForbiddenResponse();
        }
        try {
            models = configActions.getConfigs(context, descriptorName);
        } catch (final AlertException e) {
            logger.error("Was not able to find configurations with the context {}, and descriptorName {}", context, descriptorName);
            return responseFactory.createNotFoundResponse("Configurations not found for the context and descriptor provided");
        }

        if (models.isEmpty()) {
            return responseFactory.createNotFoundResponse("Configurations not found for the context and descriptor provided");
        }

        return responseFactory.createOkContentResponse(contentConverter.getJsonString(models));
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getConfig(@PathVariable final Long id) {
        final Optional<FieldModel> optionalModel;
        try {
            optionalModel = configActions.getConfigById(id);
        } catch (final AlertException e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createNotFoundResponse("Configuration not found for the specified id");
        }

        if (optionalModel.isPresent()) {
            final FieldModel fieldModel = optionalModel.get();
            final String permissionKey = AuthorizationManager.generatePermissionKey(fieldModel.getContext(), fieldModel.getDescriptorName());
            if (!authorizationManager.hasReadPermission(permissionKey)) {
                return responseFactory.createForbiddenResponse();
            }
            return responseFactory.createOkContentResponse(contentConverter.getJsonString(fieldModel));
        }

        return responseFactory.createNotFoundResponse("Configuration not found for the specified id");
    }

    @PostMapping
    public ResponseEntity<String> postConfig(@RequestBody(required = true) final FieldModel restModel) {
        if (restModel == null) {
            return responseFactory.createBadRequestResponse("", ResponseFactory.MISSING_REQUEST_BODY);
        }
        final String permissionKey = authorizationManager.generatePermissionKey(restModel.getContext(), restModel.getDescriptorName());
        if (!authorizationManager.hasCreatePermission(permissionKey)) {
            return responseFactory.createForbiddenResponse();
        }
        try {
            return runPostConfig(restModel);
        } catch (final AlertException e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createInternalServerErrorResponse(restModel.getId(), e.getMessage());
        }
    }

    private ResponseEntity<String> runPostConfig(final FieldModel fieldModel) throws AlertException {
        final String id = fieldModel.getId();
        if (configActions.doesConfigExist(id)) {
            return responseFactory.createConflictResponse(id, "Provided id must not be in use. To update an existing configuration, use PUT.");
        }
        try {
            final FieldModel updatedEntity = configActions.saveConfig(fieldModel);
            return responseFactory.createMessageResponse(HttpStatus.CREATED, updatedEntity.getId(), "Created");
        } catch (final AlertFieldException e) {
            return responseFactory.createFieldErrorResponse(id, "There were errors with the configuration.", e.getFieldErrors());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> putConfig(@PathVariable final Long id, @RequestBody(required = true) final FieldModel restModel) {
        if (restModel == null) {
            return responseFactory.createBadRequestResponse("", ResponseFactory.MISSING_REQUEST_BODY);
        }
        final String permissionKey = authorizationManager.generatePermissionKey(restModel.getContext(), restModel.getDescriptorName());
        if (!authorizationManager.hasWritePermission(permissionKey)) {
            return responseFactory.createForbiddenResponse();
        }
        try {
            return runPutConfig(id, restModel);
        } catch (final AlertException e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createInternalServerErrorResponse(restModel.getId(), e.getMessage());
        }
    }

    private ResponseEntity<String> runPutConfig(final Long id, final FieldModel restModel) throws AlertException {
        if (!configActions.doesConfigExist(id)) {
            return responseFactory.createBadRequestResponse(contentConverter.getStringValue(id), "No configuration with the specified id.");
        }

        try {
            final FieldModel updatedEntity = configActions.updateConfig(id, restModel);
            return responseFactory.createAcceptedResponse(updatedEntity.getId(), "Updated");
        } catch (final AlertFieldException e) {
            return responseFactory.createFieldErrorResponse(id.toString(), "There were errors with the configuration.", e.getFieldErrors());
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateConfig(@RequestBody(required = true) final FieldModel restModel) {
        if (restModel == null) {
            return responseFactory.createBadRequestResponse("", ResponseFactory.MISSING_REQUEST_BODY);
        }
        final String permissionKey = authorizationManager.generatePermissionKey(restModel.getContext(), restModel.getDescriptorName());
        if (!authorizationManager.hasCreatePermission(permissionKey) && !authorizationManager.hasWritePermission(permissionKey)) {
            return responseFactory.createForbiddenResponse();
        }
        final String id = restModel.getId();
        try {
            final String responseMessage = configActions.validateConfig(restModel, new HashMap<>());
            return responseFactory.createOkResponse(id, responseMessage);
        } catch (final AlertFieldException e) {
            return responseFactory.createFieldErrorResponse(id, e.getMessage(), e.getFieldErrors());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteConfig(@PathVariable final Long id) {

        if (null == id) {
            responseFactory.createBadRequestResponse("", "Proper ID is required for deleting.");
        }
        final String stringId = contentConverter.getStringValue(id);
        try {
            if (configActions.doesConfigExist(id)) {
                final Optional<FieldModel> fieldModel = configActions.getConfigById(id);
                if (fieldModel.isPresent()) {
                    final FieldModel model = fieldModel.get();
                    final String permissionKey = authorizationManager.generatePermissionKey(model.getContext(), model.getDescriptorName());
                    if (!authorizationManager.hasDeletePermission(permissionKey)) {
                        return responseFactory.createForbiddenResponse();
                    }
                }
                configActions.deleteConfig(id);
                return responseFactory.createAcceptedResponse(stringId, "Deleted");
            } else {
                return responseFactory.createBadRequestResponse(stringId, "No configuration with the specified id.");
            }
        } catch (final AlertException e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createInternalServerErrorResponse(stringId, e.getMessage());
        }
    }

    @PostMapping("/test")
    public ResponseEntity<String> testConfig(@RequestBody(required = true) final FieldModel restModel, @RequestParam(required = false) final String destination) {
        if (restModel == null) {
            return responseFactory.createBadRequestResponse("", ResponseFactory.MISSING_REQUEST_BODY);
        }
        final String permissionKey = authorizationManager.generatePermissionKey(restModel.getContext(), restModel.getDescriptorName());
        if (!authorizationManager.hasExecutePermission(permissionKey)) {
            return responseFactory.createForbiddenResponse();
        }
        final String id = restModel.getId();
        try {
            final String responseMessage = configActions.testConfig(restModel, destination);
            return responseFactory.createOkResponse(id, responseMessage);
        } catch (final IntegrationRestException e) {
            final String exceptionMessage = e.getMessage();
            logger.error(exceptionMessage, e);
            String message = exceptionMessage;
            if (StringUtils.isNotBlank(e.getHttpStatusMessage())) {
                message += " : " + e.getHttpStatusMessage();
            }
            return responseFactory.createMessageResponse(HttpStatus.valueOf(e.getHttpStatusCode()), id, message);
        } catch (final AlertFieldException e) {
            return responseFactory.createFieldErrorResponse(id, e.getMessage(), e.getFieldErrors());
        } catch (final AlertMethodNotAllowedException e) {
            return responseFactory.createMethodNotAllowedResponse(e.getMessage());
        } catch (final AlertException e) {
            return responseFactory.createBadRequestResponse(id, e.getMessage());
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createInternalServerErrorResponse(id, e.getMessage());
        }
    }

}
