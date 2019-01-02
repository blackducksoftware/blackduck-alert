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
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.provider.blackduck.model.BlackDuckProject;
import com.synopsys.integration.alert.web.controller.handler.ControllerHandler;

@Component
public class BlackDuckDataHandler extends ControllerHandler {
    private final Logger logger = LoggerFactory.getLogger(BlackDuckDataHandler.class);
    private final BlackDuckDataActions blackDuckDataActions;

    @Autowired
    public BlackDuckDataHandler(final ContentConverter contentConverter, final BlackDuckDataActions blackDuckDataActions) {
        super(contentConverter);
        this.blackDuckDataActions = blackDuckDataActions;
    }

    public ResponseEntity<String> getBlackDuckProjects() {
        try {
            final List<BlackDuckProject> projects = blackDuckDataActions.getBlackDuckProjects();
            final String usersJson = getContentConverter().getJsonString(projects);
            return createResponse(HttpStatus.OK, usersJson);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
