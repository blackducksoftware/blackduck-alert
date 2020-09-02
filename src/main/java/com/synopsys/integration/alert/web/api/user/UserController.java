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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
    private final AuthorizationManager authorizationManager;
    private final UserActions userActions;
    private final UserManagementDescriptorKey descriptorKey;

    @Autowired
    public UserController(AuthorizationManager authorizationManager, UserActions userActions,
        UserManagementDescriptorKey descriptorKey) {
        this.authorizationManager = authorizationManager;
        this.userActions = userActions;
        this.descriptorKey = descriptorKey;
    }

    @GetMapping
    public List<UserConfig> getAllUsers() {
        if (!hasGlobalPermission(authorizationManager::hasReadPermission, descriptorKey)) {
            throw ResponseFactory.createForbiddenException();
        }
        return userActions.getUsers();
    }

    @PostMapping
    public UserConfig createUser(@RequestBody UserConfig userModel) {
        if (!hasGlobalPermission(authorizationManager::hasCreatePermission, descriptorKey)) {
            throw ResponseFactory.createForbiddenException();
        }
        try {
            return userActions.createUser(userModel);
        } catch (AlertDatabaseConstraintException e) {
            logger.error("There was an issue with the DB: {}", e.getMessage());
            logger.debug("Cause", e);
            throw ResponseFactory.createInternalServerErrorException("There was an issue with the DB");
        } catch (AlertFieldException fieldException) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("There were errors with the configuration: %s", fieldException.getFlattenedErrorMessages()));
        }

    }

    @PutMapping(value = "/{userId}")
    public UserConfig updateUser(@PathVariable Long userId, @RequestBody UserConfig userModel) {
        if (!hasGlobalPermission(authorizationManager::hasWritePermission, descriptorKey)) {
            throw ResponseFactory.createForbiddenException();
        }
        try {
            return userActions.updateUser(userId, userModel);
            //return responseFactory.createCreatedResponse(ResponseFactory.EMPTY_ID, "User Updated.");
        } catch (AlertDatabaseConstraintException e) {
            logger.error("There was an issue with the DB: {}", e.getMessage());
            logger.debug("Cause", e);
            throw ResponseFactory.createInternalServerErrorException("There was an issue with the DB");
        } catch (AlertFieldException fieldException) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("There were errors with the configuration: %s", fieldException.getFlattenedErrorMessages()));
        }
    }

    @DeleteMapping(value = "/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        if (!hasGlobalPermission(authorizationManager::hasDeletePermission, descriptorKey)) {
            throw ResponseFactory.createForbiddenException();
        }
        try {
            userActions.deleteUser(userId);
        } catch (AlertForbiddenOperationException ex) {
            throw ResponseFactory.createForbiddenException(ex.getMessage());
        }
    }
}
