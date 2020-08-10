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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.accessor.AuthorizationUtility;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.exception.AlertForbiddenOperationException;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.common.util.BitwiseUtil;
import com.synopsys.integration.alert.web.model.PermissionModel;
import com.synopsys.integration.alert.web.model.RolePermissionModel;

@Component
public class RoleActions {
    private static final String FIELD_KEY_ROLE_NAME = "roleName";
    private final AuthorizationUtility authorizationUtility;
    private final AuthorizationManager authorizationManager;
    private final DescriptorMap descriptorMap;

    @Autowired
    public RoleActions(AuthorizationUtility authorizationUtility, AuthorizationManager authorizationManager, DescriptorMap descriptorMap, List<DescriptorKey> descriptorKeys) {
        this.authorizationUtility = authorizationUtility;
        this.authorizationManager = authorizationManager;
        this.descriptorMap = descriptorMap;
    }

    public Collection<RolePermissionModel> getRoles() {
        return authorizationUtility.getRoles().stream()
                   .map(this::convertUserRoleModel)
                   .collect(Collectors.toList());
    }

    public UserRoleModel createRole(RolePermissionModel rolePermissionModel) throws AlertDatabaseConstraintException, AlertFieldException, AlertConfigurationException {
        String roleName = rolePermissionModel.getRoleName();
        List<AlertFieldStatus> fieldErrors = new ArrayList<>();
        validateCreationRoleName(fieldErrors, roleName);

        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        Set<PermissionModel> permissions = rolePermissionModel.getPermissions();
        validatePermissions(permissions);
        PermissionMatrixModel permissionMatrixModel = convertToPermissionMatrixModel(permissions);
        UserRoleModel userRoleModel = authorizationUtility.createRoleWithPermissions(roleName, permissionMatrixModel);
        authorizationManager.loadPermissionsIntoCache();
        return userRoleModel;
    }

    public UserRoleModel updateRole(Long roleId, RolePermissionModel rolePermissionModel) throws AlertDatabaseConstraintException, AlertConfigurationException {
        String roleName = rolePermissionModel.getRoleName();
        authorizationUtility.updateRoleName(roleId, roleName);
        Set<PermissionModel> permissions = rolePermissionModel.getPermissions();
        validatePermissions(permissions);
        PermissionMatrixModel permissionMatrixModel = convertToPermissionMatrixModel(permissions);
        PermissionMatrixModel updatedPermissionsMatrixModel = authorizationUtility.updatePermissionsForRole(roleName, permissionMatrixModel);
        authorizationManager.loadPermissionsIntoCache();
        return new UserRoleModel(roleId, roleName, true, updatedPermissionsMatrixModel);
    }

    public void deleteRole(Long roleId) throws AlertForbiddenOperationException {
        authorizationUtility.deleteRole(roleId);
        authorizationManager.loadPermissionsIntoCache();
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

    private void validateRequiredField(String fieldKey, List<AlertFieldStatus> fieldErrors, String fieldValue) {
        if (StringUtils.isBlank(fieldValue)) {
            fieldErrors.add(AlertFieldStatus.error(fieldKey, "This field is required."));
        }
    }

    private void validateCreationRoleName(List<AlertFieldStatus> fieldErrors, String roleName) {
        validateRequiredField(FIELD_KEY_ROLE_NAME, fieldErrors, roleName);
        boolean exists = authorizationUtility.doesRoleNameExist(roleName);
        if (exists) {
            fieldErrors.add(AlertFieldStatus.error(FIELD_KEY_ROLE_NAME, "A user with that role name already exists."));
        }
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
