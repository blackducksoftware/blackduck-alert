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
package com.synopsys.integration.alert.web.provider.blackduck;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.provider.blackduck.model.BlackDuckProject;
import com.synopsys.integration.alert.web.controller.BaseController;
import com.synopsys.integration.alert.web.controller.ResponseFactory;

@RestController
@RequestMapping(BaseController.BASE_PATH + "/blackduck")
public class BlackDuckDataController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(BlackDuckDataController.class);

    private final ResponseFactory responseFactory;
    private BlackDuckDataActions blackDuckDataActions;
    private ContentConverter contentConverter;

    @Autowired
    public BlackDuckDataController(final ResponseFactory responseFactory, final BlackDuckDataActions blackDuckDataActions, final ContentConverter contentConverter) {
        this.responseFactory = responseFactory;
        this.blackDuckDataActions = blackDuckDataActions;
        this.contentConverter = contentConverter;
    }

    @GetMapping(value = "/projects")
    public ResponseEntity<String> getProjects() {
        try {
            final List<BlackDuckProject> projects = blackDuckDataActions.getBlackDuckProjects();
            final String usersJson = contentConverter.getJsonString(projects);
            return responseFactory.createResponse(HttpStatus.OK, usersJson);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
