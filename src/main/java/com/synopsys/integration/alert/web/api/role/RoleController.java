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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.BaseResourceController;
import com.synopsys.integration.alert.common.rest.api.ReadAllController;
import com.synopsys.integration.alert.common.rest.api.ValidateController;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.web.api.config.ConfigController;
import com.synopsys.integration.alert.web.common.BaseController;

@RestController
@RequestMapping(RoleController.ROLE_BASE_PATH)
public class RoleController extends BaseController implements ReadAllController<MultiRolePermissionModel>, BaseResourceController<RolePermissionModel>, ValidateController<RolePermissionModel> {
    public static final String ROLE_BASE_PATH = ConfigController.CONFIGURATION_PATH + "/role";
    private final RoleActions roleActions;

    @Autowired
    public RoleController(RoleActions roleActions) {
        //TODO get rid of AuthorizationManager and DescriptorKey if not needed
        this.roleActions = roleActions;
    }

    @Override
    public RolePermissionModel create(RolePermissionModel resource) {
        return ResponseFactory.createContentResponseFromAction(roleActions.create(resource));
    }

    @Override
    public RolePermissionModel getOne(Long id) {
        return ResponseFactory.createContentResponseFromAction(roleActions.getOne(id));
    }

    @Override
    public void update(Long id, RolePermissionModel resource) {
        ResponseFactory.createResponseFromAction(roleActions.update(id, resource));
    }

    @Override
    public void delete(Long id) {
        ResponseFactory.createResponseFromAction(roleActions.delete(id));
    }

    @Override
    public MultiRolePermissionModel getAll() {
        return ResponseFactory.createContentResponseFromAction(roleActions.getAll());
    }

    @Override
    public ValidationResponseModel validate(RolePermissionModel requestBody) {
        return ResponseFactory.createContentResponseFromAction(roleActions.validate(requestBody));
    }

    /*
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
     */

}
