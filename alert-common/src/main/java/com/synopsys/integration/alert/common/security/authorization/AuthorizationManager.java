/**
 * alert-common
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
package com.synopsys.integration.alert.common.security.authorization;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.enumeration.PermissionKeys;
import com.synopsys.integration.alert.common.enumeration.UserRole;
import com.synopsys.integration.alert.common.persistence.accessor.AuthorizationUtil;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;

@Component
public class AuthorizationManager {
    private final AuthorizationUtil authorizationUtil;
    private final Map<String, PermissionMatrixModel> permissionCache;

    @Autowired
    public AuthorizationManager(AuthorizationUtil authorizationUtil) {
        this.authorizationUtil = authorizationUtil;
        permissionCache = new HashMap<>();
    }

    public static final String generatePermissionKey(String context, String descriptorName) {
        return String.format("%s.%s", context.trim().toLowerCase(), descriptorName.trim().toLowerCase());
    }

    public final Set<AccessOperation> getOperations(String permissionKey) {
        Collection<String> roleNames = getCurrentUserRoleNames();
        return roleNames.stream()
                   .filter(permissionCache::containsKey)
                   .map(permissionCache::get)
                   .map(object -> object.getOperations(permissionKey))
                   .flatMap(Collection::stream)
                   .collect(Collectors.toSet());
    }

    public final boolean hasPermissions(String permissionKey) {
        Collection<String> roleNames = getCurrentUserRoleNames();
        return roleNames.stream()
                   .anyMatch(name -> permissionCache.containsKey(name) && permissionCache.get(name).hasPermissions(permissionKey));
    }

    public final boolean isReadOnly(String permissionKey) {
        Collection<String> roleNames = getCurrentUserRoleNames();
        return roleNames.stream()
                   .allMatch(name -> permissionCache.containsKey(name) && permissionCache.get(name).isReadOnly(permissionKey));
    }

    public final boolean hasAlertRole() {
        final EnumSet<UserRole> allowedRoles = EnumSet.allOf(UserRole.class);
        return getCurrentUserRoleNames().stream()
                   .map(UserRole::findUserRole)
                   .filter(Optional::isPresent)
                   .map(Optional::get)
                   .anyMatch(allowedRoles::contains);
    }

    public final boolean anyReadPermission(String... permissionKeys) {
        return currentUserAnyPermission(AccessOperation.READ, permissionKeys);
    }

    public final boolean hasCreatePermission(String permissionKey) {
        return currentUserHasPermission(AccessOperation.CREATE, permissionKey);
    }

    public final boolean hasDeletePermission(String permissionKey) {
        return currentUserHasPermission(AccessOperation.DELETE, permissionKey);
    }

    public final boolean hasReadPermission(PermissionKeys permissionKey) {
        return currentUserHasPermission(AccessOperation.READ, permissionKey.getDatabaseKey());
    }

    public final boolean hasReadPermission(String permissionKey) {
        return currentUserHasPermission(AccessOperation.READ, permissionKey);
    }

    public final boolean hasWritePermission(String permissionKey) {
        return currentUserHasPermission(AccessOperation.WRITE, permissionKey);
    }

    public final boolean hasExecutePermission(PermissionKeys permissionKey) {
        return currentUserHasPermission(AccessOperation.EXECUTE, permissionKey.getDatabaseKey());
    }

    public final boolean hasExecutePermission(String permissionKey) {
        return currentUserHasPermission(AccessOperation.EXECUTE, permissionKey);
    }

    private boolean currentUserAnyPermission(AccessOperation operation, String... permissionKeys) {
        Collection<String> roleNames = getCurrentUserRoleNames();
        return roleNames.stream()
                   .anyMatch(name -> permissionCache.containsKey(name) && permissionCache.get(name).anyPermissionMatch(operation, permissionKeys));
    }

    private boolean currentUserHasPermission(AccessOperation operation, String permissionKey) {
        Collection<String> roleNames = getCurrentUserRoleNames();
        return roleNames.stream()
                   .anyMatch(name -> permissionCache.containsKey(name) && permissionCache.get(name).hasPermission(permissionKey, operation));
    }

    private Set<String> getCurrentUserRoleNames() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null == authentication || !authentication.isAuthenticated()) {
            return Set.of();
        }
        return authentication.getAuthorities().stream()
                   .map(GrantedAuthority::getAuthority)
                   .filter(role -> role.startsWith(UserModel.ROLE_PREFIX))
                   .map(role -> StringUtils.substringAfter(role, UserModel.ROLE_PREFIX)).collect(Collectors.toSet());
    }

    public final void loadPermissionsIntoCache() {
        Collection<UserRoleModel> roles = authorizationUtil.createRoleModels();
        permissionCache.putAll(roles.stream().collect(Collectors.toMap(UserRoleModel::getName, UserRoleModel::getPermissions)));
    }
}
