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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.config.DescriptorActionApi;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.config.actions.DescriptorConfigActions;
import com.synopsys.integration.alert.web.controller.handler.ControllerHandler;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.alert.web.model.ResponseBodyBuilder;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class ConfigControllerHandler extends ControllerHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DescriptorConfigActions descriptorConfigActions;

    public ConfigControllerHandler(final ContentConverter contentConverter, final DescriptorConfigActions descriptorConfigActions) {
        super(contentConverter);
        this.descriptorConfigActions = descriptorConfigActions;
    }

    public List<? extends Config> getConfig(final Long id, final DescriptorActionApi descriptor) {
        try {
            return descriptorConfigActions.getConfig(id, descriptor);
        } catch (final AlertException e) {
            logger.error(e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    public ResponseEntity<String> postConfig(final Config restModel, final DescriptorActionApi descriptor) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing");
        }
        if (!descriptorConfigActions.doesConfigExist(restModel.getId(), descriptor)) {
            try {
                descriptorConfigActions.validateConfig(restModel, descriptor, new HashMap<>());
                final DatabaseEntity updatedEntity = descriptorConfigActions.saveConfig(restModel, descriptor);
                return createResponse(HttpStatus.CREATED, updatedEntity.getId(), "Created");
            } catch (final AlertFieldException e) {
                final ResponseBodyBuilder responseBuilder = new ResponseBodyBuilder(getContentConverter().getLongValue(restModel.getId()), "There were errors with the configuration.");
                responseBuilder.putErrors(e.getFieldErrors());
                return new ResponseEntity<>(responseBuilder.build(), HttpStatus.BAD_REQUEST);
            }
        }
        return createResponse(HttpStatus.CONFLICT, restModel.getId(), "Provided id must not be in use. To update an existing configuration, use PUT.");
    }

    public ResponseEntity<String> putConfig(final Config restModel, final DescriptorActionApi descriptor) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing");
        }
        if (descriptorConfigActions.doesConfigExist(restModel.getId(), descriptor)) {
            try {
                descriptorConfigActions.validateConfig(restModel, descriptor, new HashMap<>());
                try {
                    final DatabaseEntity updatedEntity = descriptorConfigActions.updateConfig(restModel, descriptor);
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

    public ResponseEntity<String> deleteConfig(final Long id, final DescriptorActionApi descriptor) {
        if (id != null && descriptorConfigActions.doesConfigExist(id, descriptor)) {
            descriptorConfigActions.deleteConfig(id, descriptor);
            return createResponse(HttpStatus.ACCEPTED, id, "Deleted");
        }
        return createResponse(HttpStatus.BAD_REQUEST, id, "No configuration with the specified id.");
    }

    public ResponseEntity<String> validateConfig(final Config restModel, final DescriptorActionApi descriptor) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing");
        }
        try {
            final String responseMessage = descriptorConfigActions.validateConfig(restModel, descriptor, new HashMap<>());
            return createResponse(HttpStatus.OK, restModel.getId(), responseMessage);
        } catch (final AlertFieldException e) {
            final ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder(getContentConverter().getLongValue(restModel.getId()), e.getMessage());
            responseBodyBuilder.putErrors(e.getFieldErrors());
            final String responseBody = responseBodyBuilder.build();
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<String> testConfig(final Config restModel, final DescriptorActionApi descriptor) {
        return testConfig(restModel, null, descriptor);
    }

    public ResponseEntity<String> testConfig(final Config restModel, final String destination, final DescriptorActionApi descriptor) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing");
        }
        try {
            //            final TestConfigModel testConfig = new TestConfigModel(restModel, destination);
            final TestConfigModel testConfig = descriptor.createTestConfigModel(restModel, destination);
            final String responseMessage = descriptorConfigActions.testConfig(testConfig, descriptor);
            return createResponse(HttpStatus.OK, restModel.getId(), responseMessage);
        } catch (final IntegrationRestException e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.valueOf(e.getHttpStatusCode()), restModel.getId(), e.getHttpStatusMessage() + " : " + e.getMessage());
        } catch (final AlertFieldException e) {
            final ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder(getContentConverter().getLongValue(restModel.getId()), e.getMessage());
            responseBodyBuilder.putErrors(e.getFieldErrors());
            final String responseBody = responseBodyBuilder.build();
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        } catch (final AlertException e) {
            final ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder(getContentConverter().getLongValue(restModel.getId()), e.getMessage());
            final String responseBody = responseBodyBuilder.build();
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        } catch (final IntegrationException e) {
            // FIXME An IntegrationException is too generic to possibly know whether a method is allowed or not. This should be supported through a custom exception (e.g. UnsupportedAlertMethodException).
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.METHOD_NOT_ALLOWED, restModel.getId(), e.getMessage());
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, restModel.getId(), e.getMessage());
        }
    }
}
