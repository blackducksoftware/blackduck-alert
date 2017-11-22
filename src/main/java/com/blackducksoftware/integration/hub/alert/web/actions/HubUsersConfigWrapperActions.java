/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.web.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.datasource.entity.HubUsersEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.HubUsersRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.manager.HubUserManager;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.model.HubUsersConfigWrapper;
import com.blackducksoftware.integration.hub.alert.web.model.ProjectVersionConfigWrapper;
import com.blackducksoftware.integration.hub.alert.web.model.ResponseBodyBuilder;

@Component
public class HubUsersConfigWrapperActions {
    private final Logger logger = LoggerFactory.getLogger(HubUsersConfigWrapperActions.class);
    private final HubUsersRepository hubUsersRepository;
    private final HubUserManager hubUserManager;

    @Autowired
    public HubUsersConfigWrapperActions(final HubUsersRepository hubUsersRepository, final HubUserManager hubUserManager) {
        this.hubUsersRepository = hubUsersRepository;
        this.hubUserManager = hubUserManager;
    }

    public List<HubUsersConfigWrapper> getConfig(final Long id) {
        if (id != null) {
            final HubUsersConfigWrapper wrapper = createWrapperRepresentation(id);
            if (wrapper != null) {
                return Arrays.asList(wrapper);
            }
            return Collections.emptyList();
        }
        return createWrapperRepresentationList();
    }

    public ResponseEntity<String> putConfig(final HubUsersConfigWrapper restModel) {
        if (restModel == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "", "Required request body is missing " + HubUsersConfigWrapper.class.getSimpleName());
        }
        if (hubUserManager.doesConfigExist(restModel.getId())) {
            try {
                validateConfig(restModel);
                try {
                    final Long savedId = hubUserManager.saveConfig(restModel);
                    return createResponse(HttpStatus.ACCEPTED, savedId, "Updated");
                } catch (final AlertException e) {
                    logger.error(e.getMessage(), e);
                    return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, restModel.getId(), e.getMessage());
                }
            } catch (final AlertFieldException e) {
                final ResponseBodyBuilder responseBuilder = new ResponseBodyBuilder(hubUserManager.getObjectTransformer().stringToLong(restModel.getId()), "There were errors with the configuration.");
                responseBuilder.putErrors(e.getFieldErrors());
                return new ResponseEntity<>(responseBuilder.build(), HttpStatus.BAD_REQUEST);
            }
        }
        return createResponse(HttpStatus.BAD_REQUEST, restModel.getId(), "No configuration with the specified id.");
    }

    public ResponseEntity<String> doNotAllowRequestMethod(final HubUsersConfigWrapper restModel, final String requestMethod) {
        logger.debug("Attempted to {} a user configuration, but that method is not allowed: {}", requestMethod, restModel);
        return createResponse(HttpStatus.METHOD_NOT_ALLOWED, -1L, "Users must be configured through the Hub.");
    }

    public String validateConfig(final HubUsersConfigWrapper restModel) throws AlertFieldException {
        final Map<String, String> fieldErrors = new HashMap<>();
        if (StringUtils.isBlank(restModel.getId())) {
            fieldErrors.put("id", "Cannot be blank.");
        }
        if (StringUtils.isBlank(restModel.getUsername())) {
            fieldErrors.put("username", "Cannot be blank.");
        }
        if (StringUtils.isBlank(restModel.getFrequency())) {
            fieldErrors.put("frequency", "Cannot be blank.");
        } else if (!("DAILY".equals(restModel.getFrequency()) || "REAL_TIME".equals(restModel.getFrequency()))) {
            fieldErrors.put("frequency", "Not a valid frequency enum. Valid values: DAILY, REAL_TIME");
        }
        if (StringUtils.isNotBlank(restModel.getEmailConfigId()) && !StringUtils.isNumeric(restModel.getEmailConfigId())) {
            fieldErrors.put("emailConfigId", "Not an Integer.");
        }
        if (StringUtils.isNotBlank(restModel.getHipChatConfigId()) && !StringUtils.isNumeric(restModel.getHipChatConfigId())) {
            fieldErrors.put("hipChatConfigId", "Not an Integer.");
        }
        if (StringUtils.isNotBlank(restModel.getSlackConfigId()) && !StringUtils.isNumeric(restModel.getSlackConfigId())) {
            fieldErrors.put("slackConfigId", "Not an Boolean.");
        }
        if (restModel.getActive() != null) {
            logger.info("Setting Hub existence to null for user {} in order to manage synchronization.", restModel.getUsername());
            restModel.setActive(null);
        }
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    private List<HubUsersConfigWrapper> createWrapperRepresentationList() {
        final List<HubUsersEntity> hubUsers = hubUsersRepository.findAll();
        final List<HubUsersConfigWrapper> allConfigs = new ArrayList<>();
        if (hubUsers != null) {
            hubUsers.forEach(hubUser -> {
                final HubUsersConfigWrapper newWrapper = createWrapperRepresentation(hubUser.getId());
                if (newWrapper != null) {
                    allConfigs.add(newWrapper);
                }
            });
        }
        return allConfigs;
    }

    private HubUsersConfigWrapper createWrapperRepresentation(final Long id) {
        final HubUsersEntity entity = hubUsersRepository.findOne(id);
        if (entity != null) {
            final String transformedId = hubUserManager.getObjectTransformer().objectToString(id);
            final String username = entity.getUsername();
            final String frequency = hubUserManager.getHubUserFrequency(id);
            final String emailConfigId = hubUserManager.getEmailConfigId(id);
            final String hipChatConfigId = hubUserManager.getHipChatConfigId(id);
            final String slackConfigId = hubUserManager.getSlackConfigId(id);
            final String active = hubUserManager.getObjectTransformer().objectToString(entity.getActive());
            final List<ProjectVersionConfigWrapper> projectVersions = hubUserManager.getProjectVersions(id);

            return new HubUsersConfigWrapper(transformedId, username, frequency, emailConfigId, hipChatConfigId, slackConfigId, active, projectVersions);
        }
        return null;
    }

    private ResponseEntity<String> createResponse(final HttpStatus status, final String id, final String message) {
        return createResponse(status, hubUserManager.getObjectTransformer().stringToLong(id), message);
    }

    private ResponseEntity<String> createResponse(final HttpStatus status, final Long id, final String message) {
        final String responseBody = new ResponseBodyBuilder(id, message).build();
        return new ResponseEntity<>(responseBody, status);
    }

}
