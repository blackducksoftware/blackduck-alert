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
package com.synopsys.integration.alert.web.config.controller.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor.ConfigurationModel;
import com.synopsys.integration.alert.web.config.actions.DescriptorConfigActions;
import com.synopsys.integration.alert.web.controller.handler.ControllerHandler;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.ResponseBodyBuilder;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

@Component
public class ConfigControllerHandler extends ControllerHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DescriptorConfigActions descriptorConfigActions;

    @Autowired
    public ConfigControllerHandler(final ContentConverter contentConverter, final DescriptorConfigActions descriptorConfigActions) {
        super(contentConverter);
        this.descriptorConfigActions = descriptorConfigActions;
    }

    public List<FieldModel> getConfigs(final ConfigContextEnum context, final String descriptorName) {
        try {
            return descriptorConfigActions.getConfigs(context, descriptorName);
        } catch (final AlertException e) {
            logger.error("Was not able to find configurations with the context {}, and descriptorName {}", context, descriptorName);
        }
        return Collections.emptyList();
    }

    public FieldModel getConfig(final Long id) {
        try {
            return descriptorConfigActions.getConfigById(id);
        } catch (final AlertException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public ResponseEntity<String> postConfig(final FieldModel restModel, final ConfigContextEnum context) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing");
        }
        final Long id = getContentConverter().getLongValue(restModel.getId());
        try {
            if (!descriptorConfigActions.doesConfigExist(id)) {
                try {
                    descriptorConfigActions.validateConfig(restModel, new HashMap<>());
                    final ConfigurationModel updatedEntity = descriptorConfigActions.saveConfig(restModel, context);
                    return createResponse(HttpStatus.CREATED, updatedEntity.getConfigurationId(), "Created");
                } catch (final AlertFieldException e) {
                    return fieldError(id, "There were errors with the configuration.", e.getFieldErrors());
                }
            }
        } catch (final AlertException e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, restModel.getId(), e.getMessage());
        }
        return createResponse(HttpStatus.CONFLICT, id, "Provided id must not be in use. To update an existing configuration, use PUT.");
    }

    public ResponseEntity<String> putConfig(final FieldModel restModel) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing");
        }
        final Long id = getContentConverter().getLongValue(restModel.getId());
        try {
            if (descriptorConfigActions.doesConfigExist(id)) {
                try {
                    descriptorConfigActions.validateConfig(restModel, new HashMap<>());
                    final ConfigurationModel updatedEntity = descriptorConfigActions.updateConfig(restModel);
                    return createResponse(HttpStatus.ACCEPTED, updatedEntity.getConfigurationId(), "Updated");
                } catch (final AlertFieldException e) {
                    return fieldError(id, "There were errors with the configuration.", e.getFieldErrors());
                }
            }
        } catch (final AlertException e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, id, e.getMessage());
        }
        return createResponse(HttpStatus.BAD_REQUEST, id, "No configuration with the specified id.");
    }

    public ResponseEntity<String> deleteConfig(final Long id) {
        try {
            if (id != null && descriptorConfigActions.doesConfigExist(id)) {
                descriptorConfigActions.deleteConfig(id);
                return createResponse(HttpStatus.ACCEPTED, id, "Deleted");
            }
        } catch (final AlertException e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, id, e.getMessage());
        }
        return createResponse(HttpStatus.BAD_REQUEST, id, "No configuration with the specified id.");
    }

    public ResponseEntity<String> validateConfig(final FieldModel restModel) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing");
        }
        final Long id = getContentConverter().getLongValue(restModel.getId());
        try {
            final String responseMessage = descriptorConfigActions.validateConfig(restModel, new HashMap<>());
            return createResponse(HttpStatus.OK, restModel.getId(), responseMessage);
        } catch (final AlertFieldException e) {
            return fieldError(id, e.getMessage(), e.getFieldErrors());
        }
    }

    public ResponseEntity<String> testConfig(final FieldModel restModel) {
        return testConfig(restModel, null);
    }

    public ResponseEntity<String> testConfig(final FieldModel restModel, final String destination) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing");
        }
        final Long id = getContentConverter().getLongValue(restModel.getId());
        try {
            final String responseMessage = descriptorConfigActions.testConfig(restModel, destination);
            return createResponse(HttpStatus.OK, id, responseMessage);
        } catch (final IntegrationRestException e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.valueOf(e.getHttpStatusCode()), id, e.getHttpStatusMessage() + " : " + e.getMessage());
        } catch (final AlertFieldException e) {
            return fieldError(id, e.getMessage(), e.getFieldErrors());
        } catch (final AlertException e) {
            final ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder(id, e.getMessage());
            final String responseBody = responseBodyBuilder.build();
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        } catch (final IntegrationException e) {
            // FIXME An IntegrationException is too generic to possibly know whether a method is allowed or not. This should be supported through a custom exception (e.g. UnsupportedAlertMethodException).
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.METHOD_NOT_ALLOWED, id, e.getMessage());
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, id, e.getMessage());
        }
    }

    private ResponseEntity<String> fieldError(final long id, final String error, final Map<String, String> fieldErrors) {
        final ResponseBodyBuilder responseBuilder = new ResponseBodyBuilder(id, error);
        responseBuilder.putErrors(fieldErrors);
        return new ResponseEntity<>(responseBuilder.build(), HttpStatus.BAD_REQUEST);
    }

}
