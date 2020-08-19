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
package com.synopsys.integration.alert.web.api.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.exception.AlertForbiddenOperationException;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.users.UserManagementDescriptorKey;
import com.synopsys.integration.alert.web.api.config.ConfigController;
import com.synopsys.integration.alert.web.common.BaseController;

@RestController
@RequestMapping(UserController.USER_BASE_PATH)
public class UserController extends BaseController {
    public static final String USER_BASE_PATH = ConfigController.CONFIGURATION_PATH + "/user";

    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final ContentConverter contentConverter;
    private final ResponseFactory responseFactory;
    private final AuthorizationManager authorizationManager;
    private final UserActions userActions;
    private final UserManagementDescriptorKey descriptorKey;

    @Autowired
    public UserController(ContentConverter contentConverter, ResponseFactory responseFactory, AuthorizationManager authorizationManager, UserActions userActions,
        UserManagementDescriptorKey descriptorKey) {
        this.contentConverter = contentConverter;
        this.responseFactory = responseFactory;
        this.authorizationManager = authorizationManager;
        this.userActions = userActions;
        this.descriptorKey = descriptorKey;
    }

    @GetMapping
    public ResponseEntity<String> getAllUsers() {
        if (!hasGlobalPermission(authorizationManager::hasReadPermission, descriptorKey)) {
            return responseFactory.createForbiddenResponse();
        }
        return responseFactory.createOkContentResponse(contentConverter.getJsonString(userActions.getUsers()));
    }

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody UserConfig userModel) {
        if (!hasGlobalPermission(authorizationManager::hasCreatePermission, descriptorKey)) {
            return responseFactory.createForbiddenResponse();
        }
        try {
            UserConfig newUser = userActions.createUser(userModel);
            return responseFactory.createCreatedResponse(newUser.getId(), "User Created.");
        } catch (AlertDatabaseConstraintException e) {
            logger.error("There was an issue with the DB: {}", e.getMessage());
            logger.debug("Cause", e);
            return responseFactory.createInternalServerErrorResponse("", "There was an issue with the DB");
        } catch (AlertFieldException e) {
            return responseFactory.createFieldErrorResponse(ResponseFactory.EMPTY_ID, "There were errors with the configuration.", e.getFieldErrors());
        }

    }

    @PutMapping(value = "/{userId}")
    public ResponseEntity<String> updateUser(@PathVariable Long userId, @RequestBody UserConfig userModel) {
        if (!hasGlobalPermission(authorizationManager::hasWritePermission, descriptorKey)) {
            return responseFactory.createForbiddenResponse();
        }
        try {
            userActions.updateUser(userId, userModel);
            return responseFactory.createCreatedResponse(ResponseFactory.EMPTY_ID, "User Updated.");
        } catch (AlertDatabaseConstraintException e) {
            logger.error("There was an issue with the DB: {}", e.getMessage());
            logger.debug("Cause", e);
            return responseFactory.createInternalServerErrorResponse("", "There was an issue with the DB");
        } catch (AlertFieldException e) {
            return responseFactory.createFieldErrorResponse(ResponseFactory.EMPTY_ID, "There were errors with the configuration.", e.getFieldErrors());
        }

    }

    @DeleteMapping(value = "/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        if (!hasGlobalPermission(authorizationManager::hasDeletePermission, descriptorKey)) {
            return responseFactory.createForbiddenResponse();
        }
        try {
            userActions.deleteUser(userId);
            return responseFactory.createOkResponse(ResponseFactory.EMPTY_ID, "Deleted");
        } catch (AlertForbiddenOperationException ex) {
            return responseFactory.createForbiddenResponse(ex.getMessage());
        }
    }
}
