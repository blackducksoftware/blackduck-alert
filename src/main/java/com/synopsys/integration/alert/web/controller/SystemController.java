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
package com.synopsys.integration.alert.web.controller;

import java.text.ParseException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.persistence.model.SystemMessageModel;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.web.actions.SystemActions;

@RestController
public class SystemController extends BaseController {
    public static final String NO_RESOURCE_FOUND = "No resource found";
    private static final Logger logger = LoggerFactory.getLogger(SystemController.class);
    private final SystemActions systemActions;
    private final ContentConverter contentConverter;
    private final ResponseFactory responseFactory;

    @Autowired
    public SystemController(final SystemActions systemActions, final ContentConverter contentConverter, final ResponseFactory responseFactory) {
        this.systemActions = systemActions;
        this.contentConverter = contentConverter;
        this.responseFactory = responseFactory;
    }

    @GetMapping(value = "/system/messages/latest")
    public ResponseEntity<String> getLatestSystemMessages() {
        final List<SystemMessageModel> systemMessageList = systemActions.getSystemMessagesSinceStartup();
        return responseFactory.createOkContentResponse(contentConverter.getJsonString(systemMessageList));
    }

    @GetMapping(value = "/system/messages")
    public ResponseEntity<String> getSystemMessages(@RequestParam(value = "startDate", required = false) final String startDate, @RequestParam(value = "endDate", required = false) final String endDate) {
        try {
            if (StringUtils.isBlank(startDate) && StringUtils.isBlank(endDate)) {
                final List<SystemMessageModel> systemMessageList = systemActions.getSystemMessages();
                return responseFactory.createOkContentResponse(contentConverter.getJsonString(systemMessageList));
            } else if (StringUtils.isNotBlank(startDate) && StringUtils.isBlank(endDate)) {
                final List<SystemMessageModel> systemMessageList = systemActions.getSystemMessagesAfter(startDate);
                return responseFactory.createOkContentResponse(contentConverter.getJsonString(systemMessageList));
            } else if (StringUtils.isBlank(startDate) && StringUtils.isNotBlank(endDate)) {
                final List<SystemMessageModel> systemMessageList = systemActions.getSystemMessagesBefore(endDate);
                return responseFactory.createOkContentResponse(contentConverter.getJsonString(systemMessageList));
            } else {
                final List<SystemMessageModel> systemMessageList = systemActions.getSystemMessagesBetween(startDate, endDate);
                return responseFactory.createOkContentResponse(contentConverter.getJsonString(systemMessageList));
            }
        } catch (final ParseException ex) {
            logger.error("error occurred getting system messages", ex);
            return responseFactory.createMessageResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @GetMapping(value = "/system/setup/initial")
    @Deprecated
    public ResponseEntity<String> getInitialSystemSetup() {
        // FIXME: 6.0.0 remove this method
        return responseFactory.createNotFoundResponse(NO_RESOURCE_FOUND);
    }

    @PostMapping(value = "/system/setup/initial")
    @Deprecated
    public ResponseEntity<String> initialSystemSetup() {
        // FIXME: 6.0.0 remove this method
        return responseFactory.createNotFoundResponse(NO_RESOURCE_FOUND);
    }

    @GetMapping(value = "/system/setup/descriptor")
    @Deprecated
    public ResponseEntity<String> getInitialSystemSetupDescriptor() {
        return responseFactory.createNotFoundResponse(NO_RESOURCE_FOUND);
    }
}
