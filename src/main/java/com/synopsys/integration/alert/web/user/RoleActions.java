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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.accessor.AuthorizationUtility;
import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.common.util.BitwiseUtil;
import com.synopsys.integration.alert.web.model.RolePermissionsModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
@Transactional
public class RoleActions {
    private static final Logger logger = LoggerFactory.getLogger(RoleActions.class);
    private AuthorizationUtility authorizationUtility;

    @Autowired
    public RoleActions(AuthorizationUtility authorizationUtility) {
        this.authorizationUtility = authorizationUtility;
    }

    public Collection<RolePermissionsModel> getRoles() {
        return authorizationUtility.getRoles().stream()
                   .map(this::convertUserRoleModel)
                   .collect(Collectors.toList());
    }

    public Optional<RolePermissionsModel> getRole(String roleName) {
        return authorizationUtility.getRoles().stream()
                   .filter(role -> role.getName().equals(roleName))
                   .map(this::convertUserRoleModel)
                   .findFirst();
    }

    public UserRoleModel createRole(RolePermissionsModel rolePermissionsModel) throws IntegrationException {
        String roleName = rolePermissionsModel.getRoleName();
        PermissionMatrixModel permissionMatrixModel = convertToPermissionMatrixModel(rolePermissionsModel.getOperations());
        return authorizationUtility.createRoleWithPermissions(roleName, permissionMatrixModel);
    }

    public void deleteRole(String roleName) throws AlertDatabaseConstraintException {
        Optional<String> userRole = authorizationUtility.getRoles().stream()
                                        .filter(role -> role.getName().equals(roleName))
                                        .filter(UserRoleModel::isCustom)
                                        .map(UserRoleModel::getName)
                                        .findFirst();
        if (userRole.isPresent()) {
            authorizationUtility.deleteRole(userRole.get());
        }
    }

    private RolePermissionsModel convertUserRoleModel(UserRoleModel userRoleModel) {
        String roleName = userRoleModel.getName();
        PermissionMatrixModel permissionModel = userRoleModel.getPermissions();
        Map<PermissionKey, Set<String>> permissionKeyToAccess = convertPermissionMatrixModel(permissionModel);
        return new RolePermissionsModel(roleName, permissionKeyToAccess);
    }

    private Map<PermissionKey, Set<String>> convertPermissionMatrixModel(PermissionMatrixModel permissionMatrixModel) {
        Map<PermissionKey, Set<String>> permissionMatrix = new HashMap<>();
        for (Map.Entry<PermissionKey, Integer> matrixRow : permissionMatrixModel.getPermissions().entrySet()) {
            Integer accessOperations = matrixRow.getValue();
            PermissionKey permissionKey = matrixRow.getKey();

            Set<String> allowedOperations = Stream.of(AccessOperation.values())
                                                .filter(accessOperation -> BitwiseUtil.containsBits(accessOperations, accessOperation.getBit()))
                                                .map(Enum::name)
                                                .collect(Collectors.toSet());

            permissionMatrix.put(permissionKey, allowedOperations);
        }
        return permissionMatrix;
    }

    private PermissionMatrixModel convertToPermissionMatrixModel(Map<PermissionKey, Set<String>> permissionMap) {
        Map<PermissionKey, Integer> permissionMatrix = new HashMap<>();
        for (Map.Entry<PermissionKey, Set<String>> matrixRow : permissionMap.entrySet()) {
            Set<String> accessOperations = matrixRow.getValue();
            PermissionKey permissionKey = matrixRow.getKey();

            int accessOperationsBits = accessOperations.stream()
                                           .filter(accessOperation -> EnumUtils.isValidEnum(AccessOperation.class, accessOperation))
                                           .map(accessOperation -> EnumUtils.getEnum(AccessOperation.class, accessOperation))
                                           .map(AccessOperation::getBit)
                                           .reduce(BitwiseUtil::combineBits)
                                           .orElse(0);

            permissionMatrix.put(permissionKey, accessOperationsBits);
        }
        return new PermissionMatrixModel(permissionMatrix);
    }

}
