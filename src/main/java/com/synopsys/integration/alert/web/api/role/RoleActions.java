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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.action.api.AbstractResourceActions;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.common.util.BitwiseUtil;
import com.synopsys.integration.alert.component.users.UserManagementDescriptorKey;
import com.synopsys.integration.alert.web.api.role.util.PermissionModelUtil;

@Component
public class RoleActions extends AbstractResourceActions<RolePermissionModel, MultiRolePermissionModel> {
    private static final String FIELD_KEY_ROLE_NAME = "roleName";
    private final RoleAccessor roleAccessor;
    private final AuthorizationManager authorizationManager;
    private final DescriptorMap descriptorMap;

    @Autowired
    public RoleActions(UserManagementDescriptorKey userManagementDescriptorKey, RoleAccessor roleAccessor, AuthorizationManager authorizationManager, DescriptorMap descriptorMap, List<DescriptorKey> descriptorKeys) {
        super(userManagementDescriptorKey, ConfigContextEnum.GLOBAL, authorizationManager);
        this.roleAccessor = roleAccessor;
        this.authorizationManager = authorizationManager;
        this.descriptorMap = descriptorMap;
    }

    @Override
    protected ActionResponse<RolePermissionModel> createWithoutChecks(RolePermissionModel resource) {
        try {
            String roleName = resource.getRoleName();
            Set<PermissionModel> permissions = resource.getPermissions();
            PermissionMatrixModel permissionMatrixModel = PermissionModelUtil.convertToPermissionMatrixModel(permissions);
            UserRoleModel userRoleModel = authorizationManager.createRoleWithPermissions(roleName, permissionMatrixModel);
            return new ActionResponse<>(HttpStatus.OK, convertUserRoleModel(userRoleModel));
        } catch (AlertException ex) {
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("There was an issue creating the role. %s", ex.getMessage()));
        }
    }

    @Override
    protected ActionResponse<RolePermissionModel> deleteWithoutChecks(Long id) {
        Optional<UserRoleModel> existingRole = roleAccessor.getRoles(List.of(id))
                                                   .stream()
                                                   .findFirst();
        if (existingRole.isPresent()) {
            try {
                authorizationManager.deleteRole(id);
            } catch (AlertException ex) {
                return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Error deleting role: %s", ex.getMessage()));
            }
            return new ActionResponse<>(HttpStatus.NO_CONTENT);
        }
        return new ActionResponse<>(HttpStatus.NOT_FOUND);
    }

    @Override
    protected ActionResponse<MultiRolePermissionModel> readAllWithoutChecks() {
        List<RolePermissionModel> roles = roleAccessor.getRoles().stream()
                                              .map(this::convertUserRoleModel)
                                              .collect(Collectors.toList());
        return new ActionResponse<>(HttpStatus.OK, new MultiRolePermissionModel(roles));
    }

    @Override
    protected ActionResponse<RolePermissionModel> readWithoutChecks(Long id) {
        Optional<RolePermissionModel> role = findExisting(id);
        if (role.isPresent()) {
            return new ActionResponse<>(HttpStatus.OK, role.get());
        }
        return new ActionResponse<>(HttpStatus.NOT_FOUND, String.format("Role with id:%d not found.", id));
    }

    @Override
    protected ValidationActionResponse testWithoutChecks(RolePermissionModel resource) {
        return validateWithoutChecks(resource);
    }

    @Override
    protected ActionResponse<RolePermissionModel> updateWithoutChecks(Long id, RolePermissionModel resource) {
        try {
            String roleName = resource.getRoleName();
            Optional<UserRoleModel> existingRole = roleAccessor.getRoles(List.of(id))
                                                       .stream()
                                                       .findFirst();
            if (existingRole.isPresent()) {
                boolean targetRoleNameIsUsedByDifferentRole = roleAccessor.getRoles()
                                                                  .stream()
                                                                  .filter(role -> !role.getId().equals(existingRole.get().getId()))
                                                                  .anyMatch(role -> role.getName().equalsIgnoreCase(roleName));
                if (targetRoleNameIsUsedByDifferentRole) {
                    return new ActionResponse<>(HttpStatus.BAD_REQUEST, "The role name is already in use");
                }
                if (!existingRole.get().getName().equals(roleName)) {
                    authorizationManager.updateRoleName(id, roleName);
                }
                Set<PermissionModel> permissions = resource.getPermissions();
                PermissionMatrixModel permissionMatrixModel = PermissionModelUtil.convertToPermissionMatrixModel(permissions);
                authorizationManager.updatePermissionsForRole(roleName, permissionMatrixModel);
                return new ActionResponse<>(HttpStatus.NO_CONTENT);
            }
            return new ActionResponse<>(HttpStatus.NOT_FOUND, "Role not found.");
        } catch (AlertException ex) {
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @Override
    protected ValidationActionResponse validateWithoutChecks(RolePermissionModel resource) {
        ValidationResponseModel responseModel;
        List<AlertFieldStatus> alertFieldStatus = fullyValidateRoleNameField(resource);
        if (alertFieldStatus.isEmpty()) {
            responseModel = ValidationResponseModel.success("The role name is valid");
            return new ValidationActionResponse(HttpStatus.OK, responseModel);
        }
        responseModel = ValidationResponseModel.fromStatusCollection("There were problems with the role configuration", alertFieldStatus);
        return new ValidationActionResponse(HttpStatus.BAD_REQUEST, responseModel);
    }

    @Override
    protected Optional<RolePermissionModel> findExisting(Long id) {
        //TODO run tests here, see if there is more than 1 element of Set<UserRoleModel
        return roleAccessor.getRoles(List.of(id))
                   .stream()
                   .findFirst()
                   .map(this::convertUserRoleModel);
    }

    private RolePermissionModel convertUserRoleModel(UserRoleModel userRoleModel) {
        String roleName = userRoleModel.getName();
        PermissionMatrixModel permissionModel = userRoleModel.getPermissions();
        Set<PermissionModel> permissionKeyToAccess = convertPermissionMatrixModel(permissionModel);
        return new RolePermissionModel(String.valueOf(userRoleModel.getId()), roleName, permissionKeyToAccess);
    }

    private Set<PermissionModel> convertPermissionMatrixModel(PermissionMatrixModel permissionMatrixModel) {
        Set<PermissionModel> permissionMatrix = new HashSet<>();
        for (Map.Entry<PermissionKey, Integer> matrixRow : permissionMatrixModel.getPermissions().entrySet()) {
            Integer accessOperations = matrixRow.getValue();
            PermissionKey permissionKey = matrixRow.getKey();
            String descriptorDisplayName = descriptorMap.getDescriptorKey(permissionKey.getDescriptorName()).map(DescriptorKey::getDisplayName).orElse(permissionKey.getDescriptorName());

            PermissionModel permissionModel = new PermissionModel(
                descriptorDisplayName,
                permissionKey.getContext(),
                BitwiseUtil.containsBits(accessOperations, AccessOperation.CREATE.getBit()),
                BitwiseUtil.containsBits(accessOperations, AccessOperation.READ.getBit()),
                BitwiseUtil.containsBits(accessOperations, AccessOperation.WRITE.getBit()),
                BitwiseUtil.containsBits(accessOperations, AccessOperation.DELETE.getBit()),
                BitwiseUtil.containsBits(accessOperations, AccessOperation.EXECUTE.getBit()),
                BitwiseUtil.containsBits(accessOperations, AccessOperation.UPLOAD_FILE_READ.getBit()),
                BitwiseUtil.containsBits(accessOperations, AccessOperation.UPLOAD_FILE_WRITE.getBit()),
                BitwiseUtil.containsBits(accessOperations, AccessOperation.UPLOAD_FILE_DELETE.getBit())
            );
            permissionMatrix.add(permissionModel);
        }
        return permissionMatrix;
    }

    private List<AlertFieldStatus> fullyValidateRoleNameField(RolePermissionModel rolePermissionModel) {
        List<AlertFieldStatus> fieldStatuses = new ArrayList<>();
        String roleName = rolePermissionModel.getRoleName();

        validateRoleNameFieldRequired(roleName).ifPresent(fieldStatuses::add);

        Set<PermissionModel> permissions = rolePermissionModel.getPermissions();
        validatePermissions(permissions).ifPresent(fieldStatuses::add);

        if (rolePermissionModel.getId() == null) {
            boolean exists = roleAccessor.doesRoleNameExist(roleName);
            if (exists) {
                fieldStatuses.add(AlertFieldStatus.error(FIELD_KEY_ROLE_NAME, "A role with that name already exists."));
            }
        }
        return fieldStatuses;
    }

    private Optional<AlertFieldStatus> validateRoleNameFieldRequired(String fieldValue) {
        if (StringUtils.isBlank(fieldValue)) {
            return Optional.of(AlertFieldStatus.error(RoleActions.FIELD_KEY_ROLE_NAME, "This field is required."));
        }
        return Optional.empty();
    }

    private Optional<AlertFieldStatus> validatePermissions(Set<PermissionModel> permissionModels) {
        Set<PermissionKey> descriptorContexts = new HashSet<>();
        for (PermissionModel permissionModel : permissionModels) {
            PermissionKey pair = new PermissionKey(permissionModel.getContext(), permissionModel.getDescriptorName());
            if (descriptorContexts.contains(pair)) {
                return Optional.of(AlertFieldStatus.error(RoleActions.FIELD_KEY_ROLE_NAME,
                    String.format("Can't save duplicate permissions for a role. Duplicate permission for '%s' with context '%s' found.", pair.getDescriptorName(), pair.getContext())));
            } else {
                descriptorContexts.add(pair);
            }
        }
        return Optional.empty();
    }
}