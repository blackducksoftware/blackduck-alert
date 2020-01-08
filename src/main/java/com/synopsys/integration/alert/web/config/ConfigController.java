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
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.exception.AlertMethodNotAllowedException;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.web.controller.BaseController;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

@RestController
@RequestMapping(ConfigController.CONFIGURATION_PATH)
public class ConfigController extends BaseController {
    public static final String CONFIGURATION_PATH = BaseController.BASE_PATH + "/configuration";
    public static final String EXCEPTION_FORMAT_CONFIGURATIONS_NOT_FOUND_FOR_CONTEXT_AND_DESCRIPTOR = "Configurations not found for the context '%s' and descriptor '%s'.";
    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);
    private final ConfigActions configActions;
    private final ContentConverter contentConverter;
    private final ResponseFactory responseFactory;
    private final AuthorizationManager authorizationManager;
    private final DescriptorMap descriptorMap;
    private PKIXErrorResponseFactory pkixErrorResponseFactory;

    @Autowired
    public ConfigController(ConfigActions configActions, ContentConverter contentConverter, ResponseFactory responseFactory, AuthorizationManager authorizationManager,
        DescriptorMap descriptorMap, PKIXErrorResponseFactory pkixErrorResponseFactory) {
        this.configActions = configActions;
        this.contentConverter = contentConverter;
        this.responseFactory = responseFactory;
        this.authorizationManager = authorizationManager;
        this.descriptorMap = descriptorMap;
        this.pkixErrorResponseFactory = pkixErrorResponseFactory;
    }

    @GetMapping
    public ResponseEntity<String> getConfigs(@RequestParam ConfigContextEnum context, @RequestParam(required = false) String descriptorName) {
        List<FieldModel> models;
        if (!authorizationManager.hasReadPermission(context.name(), descriptorName)) {
            return responseFactory.createForbiddenResponse();
        }
        try {
            DescriptorKey descriptorKey = descriptorMap.getDescriptorKey(descriptorName).orElseThrow(() -> new AlertException("Could not find a Descriptor with the name: " + descriptorName));
            models = configActions.getConfigs(context, descriptorKey);
        } catch (AlertException e) {
            logger.error("Was not able to find configurations with the context {}, and descriptorName {} to get.", context, descriptorName);
            return responseFactory.createNotFoundResponse(String.format(EXCEPTION_FORMAT_CONFIGURATIONS_NOT_FOUND_FOR_CONTEXT_AND_DESCRIPTOR, context, descriptorName));
        }

        if (models.isEmpty()) {
            return responseFactory.createNotFoundResponse(String.format(EXCEPTION_FORMAT_CONFIGURATIONS_NOT_FOUND_FOR_CONTEXT_AND_DESCRIPTOR, context, descriptorName));
        }

        return responseFactory.createOkContentResponse(contentConverter.getJsonString(models));
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getConfig(@PathVariable Long id) {
        Optional<FieldModel> optionalModel;
        try {
            optionalModel = configActions.getConfigById(id);
        } catch (AlertException e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createNotFoundResponse("Configuration not found for the specified id");
        }

        if (optionalModel.isPresent()) {
            FieldModel fieldModel = optionalModel.get();
            if (!authorizationManager.hasReadPermission(fieldModel.getContext(), fieldModel.getDescriptorName())) {
                return responseFactory.createForbiddenResponse();
            }
            return responseFactory.createOkContentResponse(contentConverter.getJsonString(fieldModel));
        }

        return responseFactory.createNotFoundResponse("Configuration not found for the specified id");
    }

    @PostMapping
    public ResponseEntity<String> postConfig(@RequestBody(required = true) FieldModel restModel) {
        if (restModel == null) {
            return responseFactory.createBadRequestResponse("", ResponseFactory.MISSING_REQUEST_BODY);
        }
        String context = restModel.getContext();
        String descriptorName = restModel.getDescriptorName();
        if (!authorizationManager.hasCreatePermission(context, descriptorName)) {
            return responseFactory.createForbiddenResponse();
        }
        DescriptorKey descriptorKey;
        try {
            descriptorKey = descriptorMap.getDescriptorKey(descriptorName).orElseThrow(() -> new AlertException("Could not find a Descriptor with the name: " + descriptorName));
        } catch (AlertException e) {
            logger.error("Was not able to find configurations with the context {}, and descriptorName {} to update.", context, descriptorName);
            return responseFactory.createNotFoundResponse(String.format(EXCEPTION_FORMAT_CONFIGURATIONS_NOT_FOUND_FOR_CONTEXT_AND_DESCRIPTOR, context, descriptorName));
        }

        try {
            return runPostConfig(restModel, descriptorKey);
        } catch (AlertException e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createInternalServerErrorResponse(restModel.getId(), e.getMessage());
        }
    }

    private ResponseEntity<String> runPostConfig(FieldModel fieldModel, DescriptorKey descriptorKey) throws AlertException {
        String id = fieldModel.getId();
        if (configActions.doesConfigExist(id)) {
            return responseFactory.createConflictResponse(id, "Provided id must not be in use. To update an existing configuration, use PUT.");
        }
        try {
            FieldModel updatedEntity = configActions.saveConfig(fieldModel, descriptorKey);
            return responseFactory.createMessageResponse(HttpStatus.CREATED, updatedEntity.getId(), "Created");
        } catch (AlertFieldException e) {
            return responseFactory.createFieldErrorResponse(id, "There were errors with the configuration.", e.getFieldErrors());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> putConfig(@PathVariable Long id, @RequestBody(required = true) FieldModel restModel) {
        if (restModel == null) {
            return responseFactory.createBadRequestResponse("", ResponseFactory.MISSING_REQUEST_BODY);
        }
        if (!authorizationManager.hasWritePermission(restModel.getContext(), restModel.getDescriptorName())) {
            return responseFactory.createForbiddenResponse();
        }
        try {
            return runPutConfig(id, restModel);
        } catch (AlertException e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createInternalServerErrorResponse(restModel.getId(), e.getMessage());
        }
    }

    private ResponseEntity<String> runPutConfig(Long id, FieldModel restModel) throws AlertException {
        if (!configActions.doesConfigExist(id)) {
            return responseFactory.createBadRequestResponse(contentConverter.getStringValue(id), "No configuration with the specified id.");
        }

        try {
            FieldModel updatedEntity = configActions.updateConfig(id, restModel);
            return responseFactory.createAcceptedResponse(updatedEntity.getId(), "Updated");
        } catch (AlertFieldException e) {
            return responseFactory.createFieldErrorResponse(id.toString(), "There were errors with the configuration.", e.getFieldErrors());
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateConfig(@RequestBody(required = true) FieldModel restModel) {
        if (restModel == null) {
            return responseFactory.createBadRequestResponse("", ResponseFactory.MISSING_REQUEST_BODY);
        }
        if (!authorizationManager.hasCreatePermission(restModel.getContext(), restModel.getDescriptorName()) && !authorizationManager.hasWritePermission(restModel.getContext(), restModel.getDescriptorName())) {
            return responseFactory.createForbiddenResponse();
        }
        String id = restModel.getId();
        try {
            String responseMessage = configActions.validateConfig(restModel, new HashMap<>());
            return responseFactory.createOkResponse(id, responseMessage);
        } catch (AlertFieldException e) {
            return responseFactory.createFieldErrorResponse(id, e.getMessage(), e.getFieldErrors());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteConfig(@PathVariable Long id) {
        if (null == id) {
            responseFactory.createBadRequestResponse("", "Proper ID is required for deleting.");
        }
        String stringId = contentConverter.getStringValue(id);
        try {
            if (configActions.doesConfigExist(id)) {
                Optional<FieldModel> fieldModel = configActions.getConfigById(id);
                if (fieldModel.isPresent()) {
                    FieldModel model = fieldModel.get();
                    if (!authorizationManager.hasDeletePermission(model.getContext(), model.getDescriptorName())) {
                        return responseFactory.createForbiddenResponse();
                    }
                }
                configActions.deleteConfig(id);
                return responseFactory.createAcceptedResponse(stringId, "Deleted");
            } else {
                return responseFactory.createBadRequestResponse(stringId, "No configuration with the specified id.");
            }
        } catch (AlertException e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createInternalServerErrorResponse(stringId, e.getMessage());
        }
    }

    @PostMapping("/test")
    public ResponseEntity<String> testConfig(@RequestBody(required = true) FieldModel restModel, @RequestParam(required = false) String destination) {
        if (restModel == null) {
            return responseFactory.createBadRequestResponse("", ResponseFactory.MISSING_REQUEST_BODY);
        }
        if (!authorizationManager.hasExecutePermission(restModel.getContext(), restModel.getDescriptorName())) {
            return responseFactory.createForbiddenResponse();
        }
        String id = restModel.getId();
        try {
            String responseMessage = configActions.testConfig(restModel, destination);
            return responseFactory.createOkResponse(id, responseMessage);
        } catch (IntegrationRestException e) {
            String exceptionMessage = e.getMessage();
            logger.error(exceptionMessage, e);
            String message = exceptionMessage;
            if (StringUtils.isNotBlank(e.getHttpStatusMessage())) {
                message += " : " + e.getHttpStatusMessage();
            }
            return responseFactory.createMessageResponse(HttpStatus.valueOf(e.getHttpStatusCode()), id, message);
        } catch (AlertFieldException e) {
            return responseFactory.createFieldErrorResponse(id, e.getMessage(), e.getFieldErrors());
        } catch (AlertMethodNotAllowedException e) {
            return responseFactory.createMethodNotAllowedResponse(e.getMessage());
        } catch (IntegrationException e) {
            return pkixErrorResponseFactory.createSSLExceptionResponse(id, e).orElse(responseFactory.createBadRequestResponse(id, e.getMessage()));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return pkixErrorResponseFactory.createSSLExceptionResponse(id, e).orElse(responseFactory.createInternalServerErrorResponse(id, e.getMessage()));
        }
    }

}
