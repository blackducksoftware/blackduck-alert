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
package com.synopsys.integration.alert.web.controller;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.web.actions.SystemActions;
import com.synopsys.integration.alert.web.model.ResponseBodyBuilder;
import com.synopsys.integration.alert.web.model.SystemMessageModel;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;

@RestController
public class SystemController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(SystemController.class);

    private final SystemActions systemActions;
    private ContentConverter contentConverter;
    private ResponseFactory responseFactory;

    @Autowired
    public SystemController(final SystemActions systemActions, final ContentConverter contentConverter, final ResponseFactory responseFactory) {
        this.systemActions = systemActions;
        this.contentConverter = contentConverter;
        this.responseFactory = responseFactory;
    }

    @GetMapping(value = "/system/messages/latest")
    public ResponseEntity<String> getLatestSystemMessages() {
        final List<SystemMessageModel> systemMessageList = systemActions.getSystemMessagesSinceStartup();
        return responseFactory.createResponse(HttpStatus.OK, contentConverter.getJsonString(systemMessageList));
    }

    @GetMapping(value = "/system/messages")
    public ResponseEntity<String> getSystemMessages(@RequestParam(value = "startDate", required = false) final String startDate, @RequestParam(value = "endDate", required = false) final String endDate) {
        try {
            if (StringUtils.isBlank(startDate) && StringUtils.isBlank(endDate)) {
                final List<SystemMessageModel> systemMessageList = systemActions.getSystemMessages();
                return responseFactory.createResponse(HttpStatus.OK, contentConverter.getJsonString(systemMessageList));
            } else if (StringUtils.isNotBlank(startDate) && StringUtils.isBlank(endDate)) {
                final List<SystemMessageModel> systemMessageList = systemActions.getSystemMessagesAfter(startDate);
                return responseFactory.createResponse(HttpStatus.OK, contentConverter.getJsonString(systemMessageList));
            } else if (StringUtils.isBlank(startDate) && StringUtils.isNotBlank(endDate)) {
                final List<SystemMessageModel> systemMessageList = systemActions.getSystemMessagesBefore(endDate);
                return responseFactory.createResponse(HttpStatus.OK, contentConverter.getJsonString(systemMessageList));
            } else {
                final List<SystemMessageModel> systemMessageList = systemActions.getSystemMessagesBetween(startDate, endDate);
                return responseFactory.createResponse(HttpStatus.OK, contentConverter.getJsonString(systemMessageList));
            }
        } catch (final ParseException ex) {
            logger.error("error occured getting system messages", ex);
            return responseFactory.createResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @GetMapping(value = "/system/setup/initial")
    public ResponseEntity<String> getInitialSystemSetup(final HttpServletRequest request) {
        final String contextPath = request.getServletContext().getContextPath();
        if (systemActions.isSystemInitialized()) {
            final HttpHeaders headers = new HttpHeaders();
            headers.add("Location", contextPath);
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }

        return new ResponseEntity<>(contentConverter.getJsonString(systemActions.getCurrentSystemSetup()), HttpStatus.OK);
    }

    @PostMapping(value = "/system/setup/initial")
    public ResponseEntity<String> initialSystemSetup(@RequestBody final FieldModel settingsToSave) {
        if (systemActions.isSystemInitialized()) {
            final ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder("System Setup has already occurred");
            final String responseBody = responseBodyBuilder.build();
            return new ResponseEntity<>(responseBody, HttpStatus.CONFLICT);
        }

        return saveSystemSettings(settingsToSave);
    }

    private ResponseEntity<String> saveSystemSettings(final FieldModel model) {
        final HashMap<String, String> fieldErrors = new HashMap<>();
        final FieldModel savedConfig = systemActions.saveRequiredInformation(model, fieldErrors);
        // FIXME this logic is a bit strange. We check to see if field errors is empty then pass the thing that's empty.
        //  handling the exception that validation normally throws here may be simpler to understand.
        if (fieldErrors.isEmpty()) {
            return responseFactory.createResponse(HttpStatus.OK, contentConverter.getJsonString(savedConfig));
        }

        final ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder("Invalid System Setup");
        responseBodyBuilder.putErrors(fieldErrors);
        final String responseBody = responseBodyBuilder.build();
        return responseFactory.createResponse(HttpStatus.BAD_REQUEST, responseBody);
    }

}
