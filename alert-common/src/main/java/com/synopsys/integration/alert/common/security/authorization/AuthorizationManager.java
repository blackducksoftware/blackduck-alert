/**
 * alert-common
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
package com.synopsys.integration.alert.common.security.authorization;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;

@Component
public class AuthorizationManager {
    private final RoleAccessor roleAccessor;
    private final Map<String, PermissionMatrixModel> permissionCache;

    @Autowired
    public AuthorizationManager(RoleAccessor roleAccessor) {
        this.roleAccessor = roleAccessor;
        this.permissionCache = new HashMap<>();
    }

    public final Set<Integer> getOperations(String context, String descriptorName) {
        PermissionKey permissionKey = new PermissionKey(context, descriptorName);
        Collection<String> roleNames = getCurrentUserRoleNames();
        return roleNames.stream()
                   .filter(permissionCache::containsKey)
                   .map(permissionCache::get)
                   .map(object -> object.getOperations(permissionKey))
                   .collect(Collectors.toSet());
    }

    public final boolean hasPermissions(String context, String descriptorName) {
        PermissionKey permissionKey = new PermissionKey(context, descriptorName);
        Collection<String> roleNames = getCurrentUserRoleNames();
        return roleNames.stream()
                   .filter(this::isAlertRole)
                   .anyMatch(name -> permissionCache.containsKey(name) && permissionCache.get(name).hasPermissions(permissionKey));
    }

    public final boolean isReadOnly(String context, String descriptorName) {
        PermissionKey permissionKey = new PermissionKey(context, descriptorName);
        Collection<String> roleNames = getCurrentUserRoleNames();
        return roleNames.stream()
                   .filter(this::isAlertRole)
                   .filter(permissionCache::containsKey)
                   .map(permissionCache::get)
                   .allMatch(permissions -> permissions.isReadOnly(permissionKey));
    }

    public final boolean anyReadPermission(Collection<String> contexts, Collection<String> descriptorNames) {
        Set<PermissionKey> permissionKeys = new HashSet<>();
        for (String context : contexts) {
            for (String descriptorName : descriptorNames) {
                permissionKeys.add(new PermissionKey(context, descriptorName));
            }
        }
        return anyReadPermission(permissionKeys);
    }

    public final boolean anyReadPermission(Collection<PermissionKey> permissionKeys) {
        return currentUserAnyPermission(AccessOperation.READ, permissionKeys);
    }

    public final boolean hasCreatePermission(String context, String descriptorName) {
        return currentUserHasPermission(AccessOperation.CREATE, context, descriptorName);
    }

    public final boolean hasDeletePermission(String context, String descriptorName) {
        return currentUserHasPermission(AccessOperation.DELETE, context, descriptorName);
    }

    public final boolean hasReadPermission(String context, String descriptorName) {
        return currentUserHasPermission(AccessOperation.READ, context, descriptorName);
    }

    public final boolean hasWritePermission(String context, String descriptorName) {
        return currentUserHasPermission(AccessOperation.WRITE, context, descriptorName);
    }

    public final boolean hasExecutePermission(String context, String descriptorName) {
        return currentUserHasPermission(AccessOperation.EXECUTE, context, descriptorName);
    }

    public final boolean hasUploadReadPermission(String context, String descriptorName) {
        return currentUserHasPermission(AccessOperation.UPLOAD_FILE_READ, context, descriptorName);
    }

    public final boolean hasUploadWritePermission(String context, String descriptorName) {
        return currentUserHasPermission(AccessOperation.UPLOAD_FILE_WRITE, context, descriptorName);
    }

    public final boolean hasUploadDeletePermission(String context, String descriptorName) {
        return currentUserHasPermission(AccessOperation.UPLOAD_FILE_DELETE, context, descriptorName);
    }

    public final boolean hasAllPermissions(String context, String descriptorName, AccessOperation... operations) {
        PermissionKey permissionKey = new PermissionKey(context, descriptorName);
        Collection<String> roleNames = getCurrentUserRoleNames();
        return roleNames.stream()
                   .filter(this::isAlertRole)
                   .anyMatch(name -> permissionCache.containsKey(name) && permissionCache.get(name).hasPermissions(permissionKey, operations));
    }

    private boolean isAlertRole(String roleName) {
        return roleAccessor.doesRoleNameExist(roleName);
    }

    private boolean currentUserAnyPermission(AccessOperation operation, Collection<PermissionKey> permissionKeys) {
        Collection<String> roleNames = getCurrentUserRoleNames();
        return roleNames.stream()
                   .anyMatch(name -> permissionCache.containsKey(name) && permissionCache.get(name).anyPermissionMatch(operation, permissionKeys));
    }

    private boolean currentUserHasPermission(AccessOperation operation, String context, String descriptorName) {
        PermissionKey permissionKey = new PermissionKey(context, descriptorName);
        Collection<String> roleNames = getCurrentUserRoleNames();
        return roleNames.stream()
                   .anyMatch(name -> permissionCache.containsKey(name) && permissionCache.get(name).hasPermission(permissionKey, operation));
    }

    private Set<String> getCurrentUserRoleNames() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null == authentication || !authentication.isAuthenticated()) {
            return Set.of();
        }
        return authentication.getAuthorities().stream()
                   .map(GrantedAuthority::getAuthority)
                   .filter(role -> role.startsWith(UserModel.ROLE_PREFIX))
                   .map(role -> StringUtils.substringAfter(role, UserModel.ROLE_PREFIX)).collect(Collectors.toSet());
    }

    public final void loadPermissionsIntoCache() {
        Collection<UserRoleModel> roles = roleAccessor.getRoles();
        permissionCache.putAll(roles.stream().collect(Collectors.toMap(UserRoleModel::getName, UserRoleModel::getPermissions)));
    }

}
