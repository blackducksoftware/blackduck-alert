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
package com.synopsys.integration.alert.web.security;

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

@Component
public class AuthorizationManager {

    private final AuthorizationUtil authorizationUtil;
    private final Map<Object, PermissionMatrixModel> permissionCache;

    @Autowired
    public AuthorizationManager(final AuthorizationUtil authorizationUtil) {
        this.authorizationUtil = authorizationUtil;
        permissionCache = new HashMap<>();
    }

    public boolean hasCreatePermission(final PermissionKeys permissionKey) {
        return currentUserHasPermission(permissionKey, AccessOperation.CREATE);
    }

    public boolean hasDeletePermission(final PermissionKeys permissionKey) {
        return currentUserHasPermission(permissionKey, AccessOperation.DELETE);
    }

    public boolean hasReadPermission(final PermissionKeys permissionKey) {
        return currentUserHasPermission(permissionKey, AccessOperation.READ);
    }

    public boolean hasWritePermission(final PermissionKeys permissionKey) {
        return currentUserHasPermission(permissionKey, AccessOperation.WRITE);
    }

    public boolean hasExecutePermission(final PermissionKeys permissionKey) {
        return currentUserHasPermission(permissionKey, AccessOperation.EXECUTE);
    }

    public boolean currentUserHasPermission(final PermissionKeys permissionKey, final AccessOperation operation) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final PermissionMatrixModel permissions;
        if (permissionCache.containsKey(authentication.getPrincipal())) {
            permissions = permissionCache.get(authentication.getPrincipal());
        } else {
            permissions = loadPermissionsIntoCache(authentication);
        }
        return permissions.hasPermission(permissionKey.getDatabaseKey(), operation);
    }

    public PermissionMatrixModel loadPermissionsIntoCache() {
        return loadPermissionsIntoCache(SecurityContextHolder.getContext().getAuthentication());
    }

    public PermissionMatrixModel loadPermissionsIntoCache(final Authentication authentication) {
        final PermissionMatrixModel permissions;
        if (null == authentication || !authentication.isAuthenticated()) {
            permissions = new PermissionMatrixModel(Map.of());
        } else {
            final List<String> roleNames = authentication.getAuthorities().stream()
                                               .map(GrantedAuthority::getAuthority)
                                               .filter(role -> role.startsWith(UserModel.ROLE_PREFIX))
                                               .map(role -> StringUtils.substringAfter(role, UserModel.ROLE_PREFIX)).collect(Collectors.toList());
            permissions = authorizationUtil.mergePermissionsForRoles(roleNames);
            permissionCache.put(authentication.getPrincipal().hashCode(), permissions);
        }
        return permissions;
    }

    public void removePermissionsFromCache() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null != authentication) {
            permissionCache.remove(authentication.getPrincipal().hashCode());
        }
    }
}
