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
import java.util.HashMap;
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
import org.springframework.web.server.ResponseStatusException;

import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.exception.AlertForbiddenOperationException;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.common.util.BitwiseUtil;

@Component
public class RoleActions {
    private static final String FIELD_KEY_ROLE_NAME = "roleName";
    private final RoleAccessor roleAccessor;
    private final AuthorizationManager authorizationManager;
    private final DescriptorMap descriptorMap;

    @Autowired
    public RoleActions(RoleAccessor roleAccessor, AuthorizationManager authorizationManager, DescriptorMap descriptorMap, List<DescriptorKey> descriptorKeys) {
        this.roleAccessor = roleAccessor;
        this.authorizationManager = authorizationManager;
        this.descriptorMap = descriptorMap;
    }

    public List<RolePermissionModel> getRoles() {
        return roleAccessor.getRoles().stream()
                   .map(this::convertUserRoleModel)
                   .collect(Collectors.toList());
    }

    public ValidationResponseModel validateRoleFields(RolePermissionModel rolePermissionModel) {
        return validateRoleNameFieldRequired(rolePermissionModel.getRoleName())
                   .map(requiredFieldError -> ValidationResponseModel.fromStatusCollection("There were problems with the role configuration", List.of(requiredFieldError)))
                   .orElseGet(() -> ValidationResponseModel.success("The role name is valid"));
    }

    public UserRoleModel createRole(RolePermissionModel rolePermissionModel) throws AlertDatabaseConstraintException, AlertFieldException, AlertConfigurationException {
        String roleName = rolePermissionModel.getRoleName();
        List<AlertFieldStatus> fieldErrors = fullyValidateRoleNameField(roleName);
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }

        Set<PermissionModel> permissions = rolePermissionModel.getPermissions();
        validatePermissions(permissions);
        PermissionMatrixModel permissionMatrixModel = convertToPermissionMatrixModel(permissions);
        UserRoleModel userRoleModel = authorizationManager.createRoleWithPermissions(roleName, permissionMatrixModel);
        return userRoleModel;
    }

    public UserRoleModel updateRole(Long roleId, RolePermissionModel rolePermissionModel) throws AlertDatabaseConstraintException, AlertConfigurationException, AlertFieldException {
        String roleName = rolePermissionModel.getRoleName();
        Optional<AlertFieldStatus> roleNameMissingError = validateRoleNameFieldRequired(roleName);
        if (roleNameMissingError.isPresent()) {
            throw AlertFieldException.singleFieldError(roleNameMissingError.get());
        }

        UserRoleModel existingRole = getExistingRoleOrThrow404(roleId);
        boolean targetRoleNameIsUsedByDifferentRole = roleAccessor.getRoles()
                                                          .stream()
                                                          .filter(role -> !role.getId().equals(existingRole.getId()))
                                                          .anyMatch(role -> role.getName().equalsIgnoreCase(roleName));
        if (targetRoleNameIsUsedByDifferentRole) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The role name is already in use");
        }

        if (!existingRole.getName().equals(roleName)) {
            roleAccessor.updateRoleName(roleId, roleName);
        }
        Set<PermissionModel> permissions = rolePermissionModel.getPermissions();
        validatePermissions(permissions);
        PermissionMatrixModel permissionMatrixModel = convertToPermissionMatrixModel(permissions);
        PermissionMatrixModel updatedPermissionsMatrixModel = authorizationManager.updatePermissionsForRole(roleName, permissionMatrixModel);
        return new UserRoleModel(roleId, roleName, true, updatedPermissionsMatrixModel);
    }

    public void deleteRole(Long roleId) throws AlertForbiddenOperationException {
        getExistingRoleOrThrow404(roleId);
        authorizationManager.deleteRole(roleId);
    }

    // TODO update this when response statuses are handled consistently across actions and controllers
    private UserRoleModel getExistingRoleOrThrow404(Long roleId) {
        return roleAccessor.getRoles(List.of(roleId))
                   .stream()
                   .findFirst()
                   .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
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

    private PermissionMatrixModel convertToPermissionMatrixModel(Set<PermissionModel> permissionModels) {
        Map<PermissionKey, Integer> permissionMatrix = new HashMap<>();
        for (PermissionModel permissionModel : permissionModels) {
            String descriptorKey = permissionModel.getDescriptorName();
            String context = permissionModel.getContext();
            PermissionKey permissionKey = new PermissionKey(context, descriptorKey);

            int accessOperationsBits = 0;
            if (permissionModel.isCreate()) {
                accessOperationsBits = BitwiseUtil.combineBits(accessOperationsBits, AccessOperation.CREATE.getBit());
            }
            if (permissionModel.isRead()) {
                accessOperationsBits = BitwiseUtil.combineBits(accessOperationsBits, AccessOperation.READ.getBit());
            }
            if (permissionModel.isDelete()) {
                accessOperationsBits = BitwiseUtil.combineBits(accessOperationsBits, AccessOperation.DELETE.getBit());
            }
            if (permissionModel.isExecute()) {
                accessOperationsBits = BitwiseUtil.combineBits(accessOperationsBits, AccessOperation.EXECUTE.getBit());
            }
            if (permissionModel.isWrite()) {
                accessOperationsBits = BitwiseUtil.combineBits(accessOperationsBits, AccessOperation.WRITE.getBit());
            }
            if (permissionModel.isUploadDelete()) {
                accessOperationsBits = BitwiseUtil.combineBits(accessOperationsBits, AccessOperation.UPLOAD_FILE_DELETE.getBit());
            }
            if (permissionModel.isUploadRead()) {
                accessOperationsBits = BitwiseUtil.combineBits(accessOperationsBits, AccessOperation.UPLOAD_FILE_READ.getBit());
            }
            if (permissionModel.isUploadWrite()) {
                accessOperationsBits = BitwiseUtil.combineBits(accessOperationsBits, AccessOperation.UPLOAD_FILE_WRITE.getBit());
            }

            permissionMatrix.put(permissionKey, accessOperationsBits);
        }
        return new PermissionMatrixModel(permissionMatrix);
    }

    private List<AlertFieldStatus> fullyValidateRoleNameField(String roleName) {
        List<AlertFieldStatus> fieldStatuses = new ArrayList<>();
        validateRoleNameFieldRequired(roleName)
            .ifPresent(fieldStatuses::add);
        boolean exists = roleAccessor.doesRoleNameExist(roleName);
        if (exists) {
            fieldStatuses.add(AlertFieldStatus.error(FIELD_KEY_ROLE_NAME, "A role with that name already exists."));
        }
        return fieldStatuses;
    }

    private Optional<AlertFieldStatus> validateRoleNameFieldRequired(String fieldValue) {
        if (StringUtils.isBlank(fieldValue)) {
            return Optional.of(AlertFieldStatus.error(RoleActions.FIELD_KEY_ROLE_NAME, "This field is required."));
        }
        return Optional.empty();
    }

    private void validatePermissions(Set<PermissionModel> permissionModels) throws AlertConfigurationException {
        Set<PermissionKey> descriptorContexts = new HashSet<>();
        for (PermissionModel permissionModel : permissionModels) {
            PermissionKey pair = new PermissionKey(permissionModel.getContext(), permissionModel.getDescriptorName());
            if (descriptorContexts.contains(pair)) {
                throw new AlertConfigurationException(String.format("Can't save duplicate permissions for a role. Duplicate permission for '%s' with context '%s' found.", pair.getDescriptorName(), pair.getContext()));
            } else {
                descriptorContexts.add(pair);
            }
        }
    }

}
