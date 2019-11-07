/**
 * alert-database
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
package com.synopsys.integration.alert.database.api;

import java.util.ArrayList;
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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.descriptor.accessor.AuthorizationUtility;
import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.database.authorization.AccessOperationEntity;
import com.synopsys.integration.alert.database.authorization.AccessOperationRepository;
import com.synopsys.integration.alert.database.authorization.PermissionMatrixRelation;
import com.synopsys.integration.alert.database.authorization.PermissionMatrixRepository;
import com.synopsys.integration.alert.database.configuration.ConfigContextEntity;
import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.database.user.RoleEntity;
import com.synopsys.integration.alert.database.user.RoleRepository;
import com.synopsys.integration.alert.database.user.UserRoleRelation;
import com.synopsys.integration.alert.database.user.UserRoleRepository;

@Component
@Transactional
public class DefaultAuthorizationUtility implements AuthorizationUtility {
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PermissionMatrixRepository permissionMatrixRepository;
    private final AccessOperationRepository accessOperationRepository;
    private final RegisteredDescriptorRepository registeredDescriptorRepository;
    private final ConfigContextRepository configContextRepository;

    @Autowired
    public DefaultAuthorizationUtility(RoleRepository roleRepository, UserRoleRepository userRoleRepository, PermissionMatrixRepository permissionMatrixRepository, AccessOperationRepository accessOperationRepository,
        RegisteredDescriptorRepository registeredDescriptorRepository, ConfigContextRepository configContextRepository) {
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.permissionMatrixRepository = permissionMatrixRepository;
        this.accessOperationRepository = accessOperationRepository;
        this.registeredDescriptorRepository = registeredDescriptorRepository;
        this.configContextRepository = configContextRepository;
    }

    @Override
    public Set<UserRoleModel> getRoles() {
        List<RoleEntity> roleList = roleRepository.findAll();
        Set<UserRoleModel> userRoles = new LinkedHashSet<>();
        for (RoleEntity entity : roleList) {
            userRoles.add(new UserRoleModel(entity.getRoleName(), entity.getCustom(), readPermissionsForRole(entity.getId())));
        }
        return userRoles;
    }

    @Override
    public Set<UserRoleModel> getRoles(Collection<Long> roleIds) {
        Set<UserRoleModel> userRoles = new LinkedHashSet<>();
        for (Long roleId : roleIds) {
            roleRepository.findById(roleId)
                .ifPresent(role -> userRoles.add(new UserRoleModel(role.getRoleName(), role.getCustom(), readPermissionsForRole(roleId))));
        }
        return userRoles;
    }

    @Override
    public boolean doesRoleNameExist(String name) {
        return roleRepository.existsRoleEntityByRoleName(name);
    }

    @Override
    public UserRoleModel createRole(String roleName) throws AlertDatabaseConstraintException {
        RoleEntity dbRole = createRole(roleName, true);
        return UserRoleModel.of(dbRole.getRoleName(), dbRole.getCustom());
    }

    @Override
    public UserRoleModel createRoleWithPermissions(String roleName, PermissionMatrixModel permissionMatrix) throws AlertDatabaseConstraintException {
        RoleEntity roleEntity = createRole(roleName, true);
        List<PermissionMatrixRelation> permissions = updateRoleOperations(roleEntity, permissionMatrix);
        return new UserRoleModel(roleEntity.getRoleName(), roleEntity.getCustom(), createModelFromPermission(permissions));
    }

    @Override
    public PermissionMatrixModel updatePermissionsForRole(String roleName, PermissionMatrixModel permissionMatrix) throws AlertDatabaseConstraintException {
        RoleEntity roleEntity = roleRepository.findByRoleName(roleName)
                                    .orElseThrow(() -> new AlertDatabaseConstraintException("No role exists with name: " + roleName));
        List<PermissionMatrixRelation> permissions = updateRoleOperations(roleEntity, permissionMatrix);
        return createModelFromPermission(permissions);
    }

    @Override
    public void deleteRole(String roleName) throws AlertDatabaseConstraintException {
        Optional<RoleEntity> foundRole = roleRepository.findByRoleName(roleName);
        if (foundRole.isPresent()) {
            RoleEntity roleEntity = foundRole.get();
            if (Boolean.FALSE.equals(roleEntity.getCustom())) {
                throw new AlertDatabaseConstraintException("Cannot delete the role '" + roleName + "' because it is not a custom role");
            }
            // Deletion cascades to permissions
            roleRepository.deleteById(roleEntity.getId());
        }
    }

    @Override
    public void updateUserRoles(Long userId, Collection<UserRoleModel> roles) {
        if (null != userId) {
            userRoleRepository.deleteAllByUserId(userId);

            if (null != roles && !roles.isEmpty()) {
                Collection<String> roleNames = roles.stream().map(UserRoleModel::getName).collect(Collectors.toSet());
                List<RoleEntity> roleEntities = roleRepository.findRoleEntitiesByRoleNames(roleNames);
                List<UserRoleRelation> roleRelations = new LinkedList<>();
                for (RoleEntity role : roleEntities) {
                    roleRelations.add(new UserRoleRelation(userId, role.getId()));
                }
                userRoleRepository.saveAll(roleRelations);
            }
        }
    }

    @Override
    public PermissionMatrixModel mergePermissionsForRoles(Collection<String> roleNames) {
        List<RoleEntity> roles = roleRepository.findRoleEntitiesByRoleNames(roleNames);
        return readPermissionsForRole(roles);
    }

    @Override
    public PermissionMatrixModel readPermissionsForRole(Long roleId) {
        List<PermissionMatrixRelation> permissions = permissionMatrixRepository.findAllByRoleId(roleId);
        return this.createModelFromPermission(permissions);
    }

    private Optional<String> getRoleName(Long roleId) {
        return roleRepository.findById(roleId).map(RoleEntity::getRoleName);
    }

    private RoleEntity createRole(String roleName, Boolean custom) throws AlertDatabaseConstraintException {
        if (StringUtils.isBlank(roleName)) {
            throw new AlertDatabaseConstraintException("The field roleName must not be blank");
        }
        RoleEntity roleEntity = new RoleEntity(roleName, custom);
        return roleRepository.save(roleEntity);
    }

    private List<PermissionMatrixRelation> updateRoleOperations(RoleEntity roleEntity, PermissionMatrixModel permissionMatrix) throws AlertDatabaseConstraintException {
        List<PermissionMatrixRelation> oldPermissionsForRole = permissionMatrixRepository.findAllByRoleId(roleEntity.getId());
        if (!oldPermissionsForRole.isEmpty()) {
            permissionMatrixRepository.deleteAll(oldPermissionsForRole);
        }

        List<PermissionMatrixRelation> matrixEntries = new ArrayList<>();
        Map<PermissionKey, EnumSet<AccessOperation>> permissions = permissionMatrix.getPermissions();
        for (Map.Entry<PermissionKey, EnumSet<AccessOperation>> permission : permissions.entrySet()) {
            PermissionKey permissionKey = permission.getKey();
            ConfigContextEntity dbContext = configContextRepository.findFirstByContext(permissionKey.getContext())
                                                .orElseThrow(() -> new AlertDatabaseConstraintException("Illegal context specified for permission"));
            RegisteredDescriptorEntity registeredDescriptor = registeredDescriptorRepository.findFirstByName(permissionKey.getDescriptorName())
                                                                  .orElseThrow(() -> new AlertDatabaseConstraintException("Illegal descriptor name specified for permission"));

            Set<String> accessOperations = permission.getValue()
                                               .stream()
                                               .map(AccessOperation::name)
                                               .collect(Collectors.toSet());
            List<AccessOperationEntity> dbAccessOperations = accessOperationRepository.findOperationEntitiesByOperationName(accessOperations);
            for (AccessOperationEntity accessOperation : dbAccessOperations) {
                PermissionMatrixRelation permissionMatrixRelation = new PermissionMatrixRelation(roleEntity.getId(), dbContext.getId(), registeredDescriptor.getId(), accessOperation.getId());
                matrixEntries.add(permissionMatrixRelation);
            }
        }
        if (!matrixEntries.isEmpty()) {
            return permissionMatrixRepository.saveAll(matrixEntries);
        }
        return List.of();
    }

    private PermissionMatrixModel readPermissionsForRole(List<RoleEntity> roles) {
        List<Long> roleIds = roles.stream().map(RoleEntity::getId).collect(Collectors.toList());
        List<PermissionMatrixRelation> permissions = permissionMatrixRepository.findAllByRoleIdIn(roleIds);
        return this.createModelFromPermission(permissions);
    }

    private PermissionMatrixModel createModelFromPermission(List<PermissionMatrixRelation> permissions) {
        Map<PermissionKey, EnumSet<AccessOperation>> permissionOperations = new HashMap<>();

        if (null != permissions) {
            for (PermissionMatrixRelation relation : permissions) {
                Optional<String> optionalContext = configContextRepository.findById(relation.getContextId()).map(ConfigContextEntity::getContext);
                Optional<String> optionalDescriptorName = registeredDescriptorRepository.findById(relation.getDescriptorId()).map(RegisteredDescriptorEntity::getName);
                if (optionalDescriptorName.isPresent() && optionalContext.isPresent()) {
                    PermissionKey permissionKey = new PermissionKey(optionalContext.get(), optionalDescriptorName.get());
                    permissionOperations.computeIfAbsent(permissionKey, ignored -> EnumSet.noneOf(AccessOperation.class));
                    accessOperationRepository.findById(relation.getAccessOperationId()).ifPresent(operation -> permissionOperations.get(permissionKey).add(AccessOperation.valueOf(operation.getOperationName())));
                }
            }
        }

        return new PermissionMatrixModel(permissionOperations);
    }

}
