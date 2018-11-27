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
package com.synopsys.integration.alert.web.controller.handler;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.web.actions.SystemActions;
import com.synopsys.integration.alert.web.model.ResponseBodyBuilder;
import com.synopsys.integration.alert.web.model.SystemMessageModel;
import com.synopsys.integration.alert.web.model.SystemSetupModel;

@Component
public class SystemHandler extends ControllerHandler {
    final Logger logger = LoggerFactory.getLogger(SystemHandler.class);
    private final SystemActions actions;

    @Autowired
    public SystemHandler(final ContentConverter contentConverter, final SystemActions actions) {
        super(contentConverter);
        this.actions = actions;
    }

    public ResponseEntity<String> getLatestMessagesSinceStartup() {
        final List<SystemMessageModel> systemMessageList = actions.getSystemMessagesSinceStartup();
        return new ResponseEntity<>(getContentConverter().getJsonString(systemMessageList), HttpStatus.OK);
    }

    public ResponseEntity<String> getSystemMessages(final String startDate, final String endDate) {
        try {
            if (StringUtils.isBlank(startDate) && StringUtils.isBlank(endDate)) {
                final List<SystemMessageModel> systemMessageList = actions.getSystemMessages();
                return new ResponseEntity<>(getContentConverter().getJsonString(systemMessageList), HttpStatus.OK);
            } else if (StringUtils.isNotBlank(startDate) && StringUtils.isBlank(endDate)) {
                final List<SystemMessageModel> systemMessageList = actions.getSystemMessagesAfter(startDate);
                return new ResponseEntity<>(getContentConverter().getJsonString(systemMessageList), HttpStatus.OK);
            } else if (StringUtils.isBlank(startDate) && StringUtils.isNotBlank(endDate)) {
                final List<SystemMessageModel> systemMessageList = actions.getSystemMessagesBefore(endDate);
                return new ResponseEntity<>(getContentConverter().getJsonString(systemMessageList), HttpStatus.OK);
            } else {
                final List<SystemMessageModel> systemMessageList = actions.getSystemMessagesBetween(startDate, endDate);
                return new ResponseEntity<>(getContentConverter().getJsonString(systemMessageList), HttpStatus.OK);
            }
        } catch (final ParseException ex) {
            logger.error("error occured getting system messages", ex);
            return createResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    public ResponseEntity<String> getCurrentSetup(final String contextPath) {
        if (actions.isSystemInitialized()) {
            final HttpHeaders headers = new HttpHeaders();
            headers.add("Location", contextPath);
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        } else {
            return new ResponseEntity<>(getContentConverter().getJsonString(actions.getCurrentSystemSetup()), HttpStatus.OK);
        }
    }

    public ResponseEntity<String> saveRequiredInformation(final SystemSetupModel requiredSystemConfiguration) {
        final ResponseEntity<String> response;
        if (actions.isSystemInitialized()) {
            final ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder("System Setup has already occurred");
            final String responseBody = responseBodyBuilder.build();
            response = new ResponseEntity<>(responseBody, HttpStatus.CONFLICT);
        } else {
            final HashMap<String, String> fieldErrors = new HashMap<>();
            final SystemSetupModel savedConfig = actions.saveRequiredInformation(requiredSystemConfiguration, fieldErrors);
            if (fieldErrors.isEmpty()) {
                response = new ResponseEntity<>(getContentConverter().getJsonString(savedConfig), HttpStatus.OK);
            } else {
                final ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder("Invalid System Setup");
                responseBodyBuilder.putErrors(fieldErrors);
                final String responseBody = responseBodyBuilder.build();
                response = new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
            }
        }
        return response;
    }
}
