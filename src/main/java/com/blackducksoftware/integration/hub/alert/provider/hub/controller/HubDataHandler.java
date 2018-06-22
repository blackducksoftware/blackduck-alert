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
package com.blackducksoftware.integration.hub.alert.provider.hub.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.provider.hub.model.HubGroup;
import com.blackducksoftware.integration.hub.alert.provider.hub.model.HubProject;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.controller.handler.ControllerHandler;
import com.blackducksoftware.integration.rest.exception.IntegrationRestException;
import com.google.gson.Gson;

@Component
public class HubDataHandler extends ControllerHandler {
    private final Logger logger = LoggerFactory.getLogger(HubDataHandler.class);
    private final Gson gson;
    private final HubDataActions hubDataActions;

    @Autowired
    public HubDataHandler(final ObjectTransformer objectTransformer, final Gson gson, final HubDataActions hubDataActions) {
        super(objectTransformer);
        this.gson = gson;
        this.hubDataActions = hubDataActions;
    }

    public ResponseEntity<String> getHubGroups() {
        try {
            final List<HubGroup> groups = hubDataActions.getHubGroups();
            final String usersJson = gson.toJson(groups);
            return createResponse(HttpStatus.OK, usersJson);
        } catch (final IntegrationRestException e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.valueOf(e.getHttpStatusCode()), e.getHttpStatusMessage() + " : " + e.getMessage());
        } catch (final IntegrationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<String> getHubProjects() {
        try {
            final List<HubProject> projects = hubDataActions.getHubProjects();
            final String usersJson = gson.toJson(projects);
            return createResponse(HttpStatus.OK, usersJson);
        } catch (final IntegrationRestException e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.valueOf(e.getHttpStatusCode()), e.getHttpStatusMessage() + " : " + e.getMessage());
        } catch (final IntegrationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
