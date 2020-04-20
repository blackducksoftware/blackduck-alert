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
package com.synopsys.integration.alert.web.user;

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
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.exception.AlertForbiddenOperationException;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.users.UserManagementDescriptorKey;
import com.synopsys.integration.alert.web.config.ConfigController;
import com.synopsys.integration.alert.web.controller.BaseController;
import com.synopsys.integration.alert.web.model.RolePermissionModel;

@RestController
@RequestMapping(RoleController.ROLE_BASE_PATH)
public class RoleController extends BaseController {
    public static final String ROLE_BASE_PATH = ConfigController.CONFIGURATION_PATH + "/role";
    private final ContentConverter contentConverter;
    private final ResponseFactory responseFactory;
    private final AuthorizationManager authorizationManager;
    private final RoleActions roleActions;
    private final UserManagementDescriptorKey descriptorKey;

    @Autowired
    public RoleController(ContentConverter contentConverter, ResponseFactory responseFactory, AuthorizationManager authorizationManager, RoleActions roleActions,
        UserManagementDescriptorKey descriptorKey) {
        this.contentConverter = contentConverter;
        this.responseFactory = responseFactory;
        this.authorizationManager = authorizationManager;
        this.roleActions = roleActions;
        this.descriptorKey = descriptorKey;
    }

    @GetMapping
    public ResponseEntity<String> getRoles() {
        if (!hasGlobalPermission(authorizationManager::hasReadPermission, descriptorKey)) {
            return responseFactory.createForbiddenResponse();
        }
        return responseFactory.createOkContentResponse(contentConverter.getJsonString(roleActions.getRoles()));
    }

    @PutMapping(value = "/{roleId}")
    public ResponseEntity<String> updateRole(@PathVariable Long roleId, @RequestBody RolePermissionModel rolePermissionModel) {
        if (!hasGlobalPermission(authorizationManager::hasWritePermission, descriptorKey)) {
            return responseFactory.createForbiddenResponse();
        }

        try {
            roleActions.updateRole(roleId, rolePermissionModel);
        } catch (AlertDatabaseConstraintException ex) {
            return responseFactory.createInternalServerErrorResponse(String.valueOf(roleId), String.format("Failed to update role: %s", ex.getMessage()));
        } catch (AlertConfigurationException e) {
            return responseFactory.createBadRequestResponse(String.valueOf(roleId), e.getMessage());
        }

        return responseFactory.createCreatedResponse(String.valueOf(roleId), "Role updated.");
    }

    @PostMapping
    public ResponseEntity<String> createRole(@RequestBody RolePermissionModel rolePermissionModel) {
        if (!hasGlobalPermission(authorizationManager::hasCreatePermission, descriptorKey)) {
            return responseFactory.createForbiddenResponse();
        }
        try {
            roleActions.createRole(rolePermissionModel);
        } catch (AlertDatabaseConstraintException ex) {
            return responseFactory.createInternalServerErrorResponse(ResponseFactory.EMPTY_ID, String.format("Failed to create the role: %s", ex.getMessage()));
        } catch (AlertFieldException e) {
            return responseFactory.createFieldErrorResponse(ResponseFactory.EMPTY_ID, "There were errors with the configuration.", e.getFieldErrors());
        } catch (AlertConfigurationException e) {
            return responseFactory.createBadRequestResponse(ResponseFactory.EMPTY_ID, e.getMessage());
        }

        return responseFactory.createCreatedResponse(ResponseFactory.EMPTY_ID, "Role created.");
    }

    @DeleteMapping(value = "/{roleId}")
    public ResponseEntity<String> deleteRole(@PathVariable Long roleId) {
        if (!hasGlobalPermission(authorizationManager::hasDeletePermission, descriptorKey)) {
            return responseFactory.createForbiddenResponse();
        }
        try {
            roleActions.deleteRole(roleId);
        } catch (AlertForbiddenOperationException ex) {
            return responseFactory.createForbiddenResponse(String.format("The role is reserved and cannot be deleted. %s", ex.getMessage()));
        }
        return responseFactory.createOkResponse(ResponseFactory.EMPTY_ID, "Role deleted.");
    }
}
