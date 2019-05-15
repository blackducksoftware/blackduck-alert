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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.enumeration.PermissionKeys;
import com.synopsys.integration.alert.common.persistence.accessor.AuthorizationUtil;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;

@Component
public class AuthorizationManager {
    private final AuthorizationUtil authorizationUtil;
    private final Map<String, PermissionMatrixModel> permissionCache;

    @Autowired
    public AuthorizationManager(final AuthorizationUtil authorizationUtil) {
        this.authorizationUtil = authorizationUtil;
        permissionCache = new HashMap<>();
    }

    public static final String generateConfigPermissionKey(String context, String descriptorName) {
        return String.format("config.%s.%s", context.toLowerCase(), descriptorName.trim().toLowerCase());
    }

    public final boolean hasPermissions(final String permissionKey) {
        Collection<String> roleNames = getCurrentUserRoleNames();
        return roleNames.stream()
                   .anyMatch(name -> permissionCache.containsKey(name) && permissionCache.get(name).hasPermissions(permissionKey));
    }

    public final boolean isReadOnly(final String permissionKey) {
        Collection<String> roleNames = getCurrentUserRoleNames();
        return roleNames.stream()
                   .allMatch(name -> permissionCache.containsKey(name) && permissionCache.get(name).isReadOnly(permissionKey));
    }

    public final boolean hasCreatePermission(final PermissionKeys permissionKey) {
        return currentUserHasPermission(permissionKey.getDatabaseKey(), AccessOperation.CREATE);
    }

    public final boolean hasCreatePermission(final String permissionKey) {
        return currentUserHasPermission(permissionKey, AccessOperation.CREATE);
    }

    public final boolean hasDeletePermission(final PermissionKeys permissionKey) {
        return currentUserHasPermission(permissionKey.getDatabaseKey(), AccessOperation.DELETE);
    }

    public final boolean hasDeletePermission(final String permissionKey) {
        return currentUserHasPermission(permissionKey, AccessOperation.DELETE);
    }

    public final boolean hasReadPermission(final PermissionKeys permissionKey) {
        return currentUserHasPermission(permissionKey.getDatabaseKey(), AccessOperation.READ);
    }

    public final boolean hasReadPermission(final String permissionKey) {
        return currentUserHasPermission(permissionKey, AccessOperation.READ);
    }

    public final boolean hasWritePermission(final PermissionKeys permissionKey) {
        return currentUserHasPermission(permissionKey.getDatabaseKey(), AccessOperation.WRITE);
    }

    public final boolean hasWritePermission(final String permissionKey) {
        return currentUserHasPermission(permissionKey, AccessOperation.WRITE);
    }

    public final boolean hasExecutePermission(final PermissionKeys permissionKey) {
        return currentUserHasPermission(permissionKey.getDatabaseKey(), AccessOperation.EXECUTE);
    }

    public final boolean hasExecutePermission(final String permissionKey) {
        return currentUserHasPermission(permissionKey, AccessOperation.EXECUTE);
    }

    private boolean currentUserHasPermission(final String permissionKey, final AccessOperation operation) {
        Collection<String> roleNames = getCurrentUserRoleNames();
        return roleNames.stream()
                   .anyMatch(name -> permissionCache.containsKey(name) && permissionCache.get(name).hasPermission(permissionKey, operation));
    }

    private Collection<String> getCurrentUserRoleNames() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null == authentication || !authentication.isAuthenticated()) {
            return List.of();
        }
        return authentication.getAuthorities().stream()
                   .map(GrantedAuthority::getAuthority)
                   .filter(role -> role.startsWith(UserModel.ROLE_PREFIX))
                   .map(role -> StringUtils.substringAfter(role, UserModel.ROLE_PREFIX)).collect(Collectors.toList());
    }

    public final void loadPermissionsIntoCache() {
        Collection<UserRoleModel> roles = authorizationUtil.createRoleModels();
        permissionCache.putAll(roles.stream().collect(Collectors.toMap(UserRoleModel::getName, UserRoleModel::getPermissions)));
    }

    public void removePermissionsFromCache() {
        permissionCache.clear();
    }
}
