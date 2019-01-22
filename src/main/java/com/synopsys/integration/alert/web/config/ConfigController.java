/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.web.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import com.synopsys.integration.alert.web.controller.BaseController;
import com.synopsys.integration.alert.web.controller.ResponseFactory;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.ResponseBodyBuilder;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;

@RestController
@RequestMapping(ConfigController.CONFIGURATION_PATH)
public class ConfigController extends BaseController {
    public static final String CONFIGURATION_PATH = BaseController.BASE_PATH + "/configuration";
    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);

    private ConfigActions configActions;
    private ContentConverter contentConverter;
    private ResponseFactory responseFactory;

    @Autowired
    public ConfigController(final ConfigActions configActions, final ContentConverter contentConverter, final ResponseFactory responseFactory) {
        this.configActions = configActions;
        this.contentConverter = contentConverter;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    public ResponseEntity<String> getConfigs(final @RequestParam ConfigContextEnum context, @RequestParam(required = false) final String descriptorName) {
        List<FieldModel> models;
        try {
            models = configActions.getConfigs(context, descriptorName);
        } catch (final AlertException e) {
            logger.error("Was not able to find configurations with the context {}, and descriptorName {}", context, descriptorName);
            return responseFactory.createNotFoundResponse("Configurations not found for the context and descriptor provided");
        }

        if (models.isEmpty()) {
            return responseFactory.createNotFoundResponse("Configurations not found for the context and descriptor provided");
        }

        return new ResponseEntity<>(contentConverter.getJsonString(models), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getConfig(@PathVariable final Long id) {
        Optional<FieldModel> optionalModel;
        try {
            optionalModel = configActions.getConfigById(id);
        } catch (final AlertException e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createNotFoundResponse("Configuration not found for the specified id");
        }

        if (optionalModel.isPresent()) {
            return new ResponseEntity<>(contentConverter.getJsonString(optionalModel.get()), HttpStatus.OK);
        }

        return responseFactory.createNotFoundResponse("Configuration not found for the specified id");
    }

    @PostMapping
    public ResponseEntity<String> postConfig(@RequestBody(required = true) final FieldModel restModel) {
        if (restModel == null) {
            return responseFactory.createBadRequestResponse("", "Required request body is missing");
        }
        String stringId = restModel.getId();
        final Long id = contentConverter.getLongValue(stringId);
        try {
            if (!configActions.doesConfigExist(id)) {
                try {
                    configActions.validateConfig(restModel, new HashMap<>());
                    final FieldModel updatedEntity = configActions.saveConfig(restModel);
                    return responseFactory.createResponse(HttpStatus.CREATED, updatedEntity.getId(), "Created");
                } catch (final AlertFieldException e) {
                    return fieldError(id, "There were errors with the configuration.", e.getFieldErrors());
                }
            } else {
                return responseFactory.createConflictResponse(stringId, "Provided id must not be in use. To update an existing configuration, use PUT.");
            }
        } catch (final AlertException e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createInternalServerErrorResponse(restModel.getId(), e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> putConfig(@PathVariable final Long id, @RequestBody(required = true) final FieldModel restModel) {
        if (restModel == null) {
            return responseFactory.createBadRequestResponse("", "Required request body is missing");
        }

        String stringId = restModel.getId();
        try {
            if (configActions.doesConfigExist(id)) {
                try {
                    configActions.validateConfig(restModel, new HashMap<>());
                    final FieldModel updatedEntity = configActions.updateConfig(id, restModel);
                    return responseFactory.createAcceptedResponse(updatedEntity.getId(), "Updated");
                } catch (final AlertFieldException e) {
                    return fieldError(id, "There were errors with the configuration.", e.getFieldErrors());
                }
            } else {
                return responseFactory.createBadRequestResponse(stringId, "No configuration with the specified id.");
            }
        } catch (final AlertException e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createInternalServerErrorResponse(stringId, e.getMessage());
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateConfig(@RequestBody(required = true) final FieldModel restModel) {
        if (restModel == null) {
            return responseFactory.createBadRequestResponse("", "Required request body is missing");
        }
        String stringId = restModel.getId();
        final Long id = contentConverter.getLongValue(stringId);
        try {
            final String responseMessage = configActions.validateConfig(restModel, new HashMap<>());
            return responseFactory.createOkResponse(stringId, responseMessage);
        } catch (final AlertFieldException e) {
            return fieldError(id, e.getMessage(), e.getFieldErrors());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteConfig(@PathVariable final Long id) {
        return controllerHandler.deleteConfig(id);
    }

    @PostMapping("/test")
    public ResponseEntity<String> testConfig(@RequestBody(required = true) final FieldModel restModel, @RequestParam(required = false) final String destination) {
        return controllerHandler.testConfig(restModel, destination);
    }

    private ResponseEntity<String> fieldError(final long id, final String error, final Map<String, String> fieldErrors) {
        final ResponseBodyBuilder responseBuilder = new ResponseBodyBuilder(String.valueOf(id), error);
        responseBuilder.putErrors(fieldErrors);
        return new ResponseEntity<>(responseBuilder.build(), HttpStatus.BAD_REQUEST);
    }
}
