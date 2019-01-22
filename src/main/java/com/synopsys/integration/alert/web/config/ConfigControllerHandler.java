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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

@Component
public class ConfigControllerHandler extends ControllerHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ConfigActions descriptorConfigActions;

    @Autowired
    public ConfigControllerHandler(final ContentConverter contentConverter, final ConfigActions descriptorConfigActions) {
        super(contentConverter);
        this.descriptorConfigActions = descriptorConfigActions;
    }

    public ResponseEntity<String> getConfigs(final ConfigContextEnum context, final String descriptorName) {

    }

    public ResponseEntity<String> getConfig(final Long id) {

    }

    public ResponseEntity<String> postConfig(final FieldModel restModel) {

    }

    public ResponseEntity<String> putConfig(final Long id, final FieldModel restModel) {

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
            return createResponse(HttpStatus.BAD_REQUEST, id, e.getMessage());
        } catch (final IntegrationException e) {
            // FIXME An IntegrationException is too generic to possibly know whether a method is allowed or not. This should be supported through a custom exception (e.g. UnsupportedAlertMethodException).
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.METHOD_NOT_ALLOWED, id, e.getMessage());
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, id, e.getMessage());
        }
    }

}
