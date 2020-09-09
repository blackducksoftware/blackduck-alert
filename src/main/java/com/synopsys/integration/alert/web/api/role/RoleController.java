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
package com.synopsys.integration.alert.web.api.role;

import java.util.List;

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

import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.exception.AlertForbiddenOperationException;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.users.UserManagementDescriptorKey;
import com.synopsys.integration.alert.web.api.config.ConfigController;
import com.synopsys.integration.alert.web.common.BaseController;

@RestController
@RequestMapping(RoleController.ROLE_BASE_PATH)
public class RoleController extends BaseController {
    public static final String ROLE_BASE_PATH = ConfigController.CONFIGURATION_PATH + "/role";
    private final AuthorizationManager authorizationManager;
    private final RoleActions roleActions;
    private final UserManagementDescriptorKey descriptorKey;

    @Autowired
    public RoleController(AuthorizationManager authorizationManager, RoleActions roleActions, UserManagementDescriptorKey descriptorKey) {
        this.authorizationManager = authorizationManager;
        this.roleActions = roleActions;
        this.descriptorKey = descriptorKey;
    }

    @GetMapping
    public MultiRolePermissionModel getRoles() {
        if (!hasGlobalPermission(authorizationManager::hasReadPermission, descriptorKey)) {
            throw ResponseFactory.createForbiddenException();
        }
        List<RolePermissionModel> allRoles = roleActions.getRoles();
        return new MultiRolePermissionModel(allRoles);
    }

    @PostMapping("/validate")
    public ValidationResponseModel validateRoleFields(@RequestBody RolePermissionModel rolePermissionModel) {
        return roleActions.validateRoleFields(rolePermissionModel);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserRoleModel createRole(@RequestBody RolePermissionModel rolePermissionModel) {
        if (!hasGlobalPermission(authorizationManager::hasCreatePermission, descriptorKey)) {
            throw ResponseFactory.createForbiddenException();
        }
        try {
            return roleActions.createRole(rolePermissionModel);
        } catch (AlertDatabaseConstraintException ex) {
            throw ResponseFactory.createInternalServerErrorException(String.format("Failed to create the role: %s", ex.getMessage()));
        } catch (AlertFieldException e) {
            throw ResponseFactory.createBadRequestException(String.format("There were errors with the configuration: %s", e.getFlattenedErrorMessages()));
        } catch (AlertConfigurationException e) {
            throw ResponseFactory.createBadRequestException(e.getMessage());
        }
    }

    @PutMapping(value = "/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRole(@PathVariable Long roleId, @RequestBody RolePermissionModel rolePermissionModel) {
        if (!hasGlobalPermission(authorizationManager::hasWritePermission, descriptorKey)) {
            throw ResponseFactory.createForbiddenException();
        }

        try {
            roleActions.updateRole(roleId, rolePermissionModel);
        } catch (AlertDatabaseConstraintException ex) {
            throw ResponseFactory.createInternalServerErrorException(String.format("Failed to update role: %s", ex.getMessage()));
        } catch (AlertFieldException e) {
            throw ResponseFactory.createBadRequestException(String.format("There were errors with the configuration: %s", e.getFlattenedErrorMessages()));
        } catch (AlertConfigurationException e) {
            throw ResponseFactory.createBadRequestException(e.getMessage());
        }
    }

    @DeleteMapping(value = "/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(@PathVariable Long roleId) {
        if (!hasGlobalPermission(authorizationManager::hasDeletePermission, descriptorKey)) {
            throw ResponseFactory.createForbiddenException();
        }
        try {
            roleActions.deleteRole(roleId);
        } catch (AlertForbiddenOperationException ex) {
            throw ResponseFactory.createForbiddenException(String.format("The role is reserved and cannot be deleted. %s", ex.getMessage()));
        }
    }

}
