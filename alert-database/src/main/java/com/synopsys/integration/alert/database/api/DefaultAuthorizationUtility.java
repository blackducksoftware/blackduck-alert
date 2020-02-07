/**
 * alert-database
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
package com.synopsys.integration.alert.database.api;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.persistence.accessor.AuthorizationUtil;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.database.authorization.AccessOperationRepository;
import com.synopsys.integration.alert.database.authorization.PermissionKeyRepository;
import com.synopsys.integration.alert.database.authorization.PermissionMatrixRelation;
import com.synopsys.integration.alert.database.authorization.PermissionMatrixRepository;
import com.synopsys.integration.alert.database.user.RoleEntity;
import com.synopsys.integration.alert.database.user.RoleRepository;
import com.synopsys.integration.alert.database.user.UserRoleRelation;
import com.synopsys.integration.alert.database.user.UserRoleRepository;

@Component
@Transactional
public class DefaultAuthorizationUtility implements AuthorizationUtil {
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PermissionMatrixRepository permissionMatrixRepository;
    private final AccessOperationRepository accessOperationRepository;
    private final PermissionKeyRepository permissionKeyRepository;

    @Autowired
    public DefaultAuthorizationUtility(final RoleRepository roleRepository, final UserRoleRepository userRoleRepository, final PermissionMatrixRepository permissionMatrixRepository, final AccessOperationRepository accessOperationRepository,
        final PermissionKeyRepository permissionKeyRepository) {
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.permissionMatrixRepository = permissionMatrixRepository;
        this.accessOperationRepository = accessOperationRepository;
        this.permissionKeyRepository = permissionKeyRepository;
    }

    @Override
    public Set<UserRoleModel> createRoleModels() {
        List<RoleEntity> roleList = roleRepository.findAll();
        final Set<UserRoleModel> userRoles = new LinkedHashSet<>();
        for (RoleEntity entity : roleList) {
            userRoles.add(UserRoleModel.of(entity.getRoleName(), readPermissionsForRole(entity.getId())));
        }
        return userRoles;
    }

    @Override
    public Set<UserRoleModel> createRoleModels(final Collection<Long> roleIds) {
        final Set<UserRoleModel> userRoles = new LinkedHashSet<>();
        for (final Long roleId : roleIds) {
            getRoleName(roleId).ifPresent(role -> userRoles.add(UserRoleModel.of(role, readPermissionsForRole(roleId))));
        }
        return userRoles;
    }

    @Override
    public void updateUserRoles(final Long userId, final Collection<UserRoleModel> roles) {
        if (null != userId) {
            userRoleRepository.deleteAllByUserId(userId);

            if (null != roles && !roles.isEmpty()) {
                final Collection<String> roleNames = roles.stream().map(UserRoleModel::getName).collect(Collectors.toSet());
                final List<RoleEntity> roleEntities = roleRepository.findRoleEntitiesByRoleNames(roleNames);
                final List<UserRoleRelation> roleRelations = new LinkedList<>();
                for (final RoleEntity role : roleEntities) {
                    roleRelations.add(new UserRoleRelation(userId, role.getId()));
                }
                userRoleRepository.saveAll(roleRelations);
            }
        }
    }

    @Override
    public PermissionMatrixModel mergePermissionsForRoles(final Collection<String> roleNames) {
        final List<RoleEntity> roles = roleRepository.findRoleEntitiesByRoleNames(roleNames);
        return readPermissionsForRole(roles);
    }

    @Override
    public PermissionMatrixModel readPermissionsForRole(final Long roleId) {
        final List<PermissionMatrixRelation> permissions = permissionMatrixRepository.findAllByRoleId(roleId);
        return this.createPermissionMatrix(permissions);
    }

    private Optional<String> getRoleName(final Long roleId) {
        return roleRepository.findById(roleId).map(RoleEntity::getRoleName);
    }

    private PermissionMatrixModel readPermissionsForRole(final List<RoleEntity> roles) {
        final List<Long> roleIds = roles.stream().map(RoleEntity::getId).collect(Collectors.toList());
        final List<PermissionMatrixRelation> permissions = permissionMatrixRepository.findAllByRoleIdIn(roleIds);
        return this.createPermissionMatrix(permissions);
    }

    private PermissionMatrixModel createPermissionMatrix(final List<PermissionMatrixRelation> permissions) {
        final Map<String, EnumSet<AccessOperation>> permissionOperations = new HashMap<>();

        if (null != permissions) {
            for (final PermissionMatrixRelation relation : permissions) {
                permissionKeyRepository.findById(relation.getPermissionKeyId()).ifPresent(key -> {
                    final String keyName = key.getKeyName();
                    permissionOperations.computeIfAbsent(keyName, ignored -> EnumSet.noneOf(AccessOperation.class));
                    accessOperationRepository.findById(relation.getAccessOperationId()).ifPresent(operation -> permissionOperations.get(keyName).add(AccessOperation.valueOf(operation.getOperationName())));
                });
            }
        }

        return new PermissionMatrixModel(permissionOperations);
    }
}
