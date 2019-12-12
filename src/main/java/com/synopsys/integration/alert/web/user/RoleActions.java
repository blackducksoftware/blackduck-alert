/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.accessor.AuthorizationUtility;
import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.common.util.BitwiseUtil;
import com.synopsys.integration.alert.web.model.PermissionModel;
import com.synopsys.integration.alert.web.model.RolePermissionModel;

@Component
@Transactional
public class RoleActions {
    private static final Logger logger = LoggerFactory.getLogger(RoleActions.class);
    private static final String FIELD_KEY_ROLE_NAME = "roleName";
    private AuthorizationUtility authorizationUtility;

    @Autowired
    public RoleActions(AuthorizationUtility authorizationUtility) {
        this.authorizationUtility = authorizationUtility;
    }

    public Collection<RolePermissionModel> getRoles() {
        return authorizationUtility.getRoles().stream()
                   .map(this::convertUserRoleModel)
                   .collect(Collectors.toList());
    }

    public UserRoleModel createRole(RolePermissionModel rolePermissionModel) throws AlertDatabaseConstraintException, AlertFieldException {
        String roleName = rolePermissionModel.getRoleName();
        Map<String, String> fieldErrors = new HashMap<>();
        validateCreationRoleName(fieldErrors, roleName);

        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }

        PermissionMatrixModel permissionMatrixModel = convertToPermissionMatrixModel(rolePermissionModel.getPermissions());
        return authorizationUtility.createRoleWithPermissions(roleName, permissionMatrixModel);
    }

    public UserRoleModel updateRole(Long roleId, RolePermissionModel rolePermissionModel) throws AlertDatabaseConstraintException {
        String roleName = rolePermissionModel.getRoleName();
        authorizationUtility.updateRoleName(roleId, roleName);
        Set<PermissionModel> permissions = rolePermissionModel.getPermissions();
        PermissionMatrixModel permissionMatrixModel = convertToPermissionMatrixModel(permissions);
        PermissionMatrixModel updatedPermissionsMatrixModel = authorizationUtility.updatePermissionsForRole(roleName, permissionMatrixModel);
        return new UserRoleModel(roleId, roleName, true, updatedPermissionsMatrixModel);
    }

    public void deleteRole(Long roleId) throws AlertDatabaseConstraintException {
        Optional<String> userRole = authorizationUtility.getRoles().stream()
                                        .filter(role -> role.getId().equals(roleId))
                                        .filter(UserRoleModel::isCustom)
                                        .map(UserRoleModel::getName)
                                        .findFirst();
        if (userRole.isPresent()) {
            authorizationUtility.deleteRole(userRole.get());
        }
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

            PermissionModel permissionModel = new PermissionModel(
                permissionKey.getDescriptorName(),
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
            String descriptorName = permissionModel.getDescriptorName();
            String context = permissionModel.getContext();
            PermissionKey permissionKey = new PermissionKey(context, descriptorName);

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

    private void validateRequiredField(String fieldKey, Map<String, String> fieldErrors, String fieldValue) {
        if (StringUtils.isBlank(fieldValue)) {
            fieldErrors.put(fieldKey, "This field is required.");
        }
    }

    private void validateCreationRoleName(Map<String, String> fieldErrors, String roleName) {
        validateRequiredField(FIELD_KEY_ROLE_NAME, fieldErrors, roleName);
        boolean exists = authorizationUtility.doesRoleNameExist(roleName);
        if (exists) {
            fieldErrors.put(FIELD_KEY_ROLE_NAME, "A user with that username already exists.");
        }
    }

}
