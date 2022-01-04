/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.security.authorization;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertForbiddenOperationException;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
public class AuthorizationManager {
    private final RoleAccessor roleAccessor;
    private final Map<String, PermissionMatrixModel> permissionCache;

    private final Logger logger = LoggerFactory.getLogger(AuthorizationManager.class);

    @Autowired
    public AuthorizationManager(RoleAccessor roleAccessor) {
        this.roleAccessor = roleAccessor;
        this.permissionCache = new HashMap<>();
        loadPermissionsIntoCache();
    }

    public final Set<Integer> getOperations(ConfigContextEnum context, DescriptorKey descriptorKey) {
        PermissionKey permissionKey = new PermissionKey(context.name(), descriptorKey.getUniversalKey());
        Collection<String> roleNames = getCurrentUserRoleNames();
        return roleNames.stream()
                   .filter(permissionCache::containsKey)
                   .map(permissionCache::get)
                   .map(object -> object.getOperations(permissionKey))
                   .collect(Collectors.toSet());
    }

    public final boolean hasPermissions(ConfigContextEnum context, DescriptorKey descriptorKey) {
        PermissionKey permissionKey = new PermissionKey(context.name(), descriptorKey.getUniversalKey());
        Collection<String> roleNames = getCurrentUserRoleNames();
        return roleNames.stream()
                   .filter(this::isAlertRole)
                   .anyMatch(name -> permissionCache.containsKey(name) && permissionCache.get(name).hasPermissions(permissionKey));
    }

    public final boolean isReadOnly(ConfigContextEnum context, DescriptorKey descriptorKey) {
        PermissionKey permissionKey = new PermissionKey(context.name(), descriptorKey.getUniversalKey());
        Collection<String> roleNames = getCurrentUserRoleNames();
        return roleNames.stream()
                   .filter(this::isAlertRole)
                   .filter(permissionCache::containsKey)
                   .map(permissionCache::get)
                   .allMatch(permissions -> permissions.isReadOnly(permissionKey));
    }

    public final boolean anyReadPermission(Collection<ConfigContextEnum> contexts, Collection<String> descriptorKeys) {
        Set<PermissionKey> permissionKeys = new HashSet<>();
        for (ConfigContextEnum context : contexts) {
            for (String descriptorKey : descriptorKeys) {
                permissionKeys.add(new PermissionKey(context.name(), descriptorKey));
            }
        }
        return anyReadPermission(permissionKeys);
    }

    public final boolean anyReadPermission(Collection<PermissionKey> permissionKeys) {
        return currentUserAnyPermission(AccessOperation.READ, permissionKeys);
    }

    @Deprecated
    public final boolean hasCreatePermission(String context, String descriptorKey) {
        return currentUserHasPermission(AccessOperation.CREATE, context, descriptorKey);
    }

    public final boolean hasCreatePermission(ConfigContextEnum context, DescriptorKey descriptorKey) {
        return currentUserHasPermission(AccessOperation.CREATE, context, descriptorKey);
    }

    @Deprecated
    public final boolean hasDeletePermission(String context, String descriptorKey) {
        return currentUserHasPermission(AccessOperation.DELETE, context, descriptorKey);
    }

    public final boolean hasDeletePermission(ConfigContextEnum context, DescriptorKey descriptorKey) {
        return currentUserHasPermission(AccessOperation.DELETE, context, descriptorKey);
    }

    @Deprecated
    public final boolean hasReadPermission(String context, String descriptorKey) {
        return currentUserHasPermission(AccessOperation.READ, context, descriptorKey);
    }

    public final boolean hasReadPermission(ConfigContextEnum context, DescriptorKey descriptorKey) {
        return currentUserHasPermission(AccessOperation.READ, context, descriptorKey);
    }

    @Deprecated
    public final boolean hasWritePermission(String context, String descriptorKey) {
        return currentUserHasPermission(AccessOperation.WRITE, context, descriptorKey);
    }

    public final boolean hasWritePermission(ConfigContextEnum context, DescriptorKey descriptorKey) {
        return currentUserHasPermission(AccessOperation.WRITE, context, descriptorKey);
    }

    @Deprecated
    public final boolean hasExecutePermission(String context, String descriptorKey) {
        return currentUserHasPermission(AccessOperation.EXECUTE, context, descriptorKey);
    }

    public final boolean hasExecutePermission(ConfigContextEnum context, DescriptorKey descriptorKey) {
        return currentUserHasPermission(AccessOperation.EXECUTE, context, descriptorKey);
    }

    public final boolean hasUploadReadPermission(ConfigContextEnum context, DescriptorKey descriptorKey) {
        return currentUserHasPermission(AccessOperation.UPLOAD_FILE_READ, context, descriptorKey);
    }

    public final boolean hasUploadWritePermission(ConfigContextEnum context, DescriptorKey descriptorKey) {
        return currentUserHasPermission(AccessOperation.UPLOAD_FILE_WRITE, context, descriptorKey);
    }

    public final boolean hasUploadDeletePermission(ConfigContextEnum context, DescriptorKey descriptorKey) {
        return currentUserHasPermission(AccessOperation.UPLOAD_FILE_DELETE, context, descriptorKey);
    }

    public final boolean hasAllPermissions(ConfigContextEnum context, DescriptorKey descriptorKey, AccessOperation... operations) {
        PermissionKey permissionKey = new PermissionKey(context.name(), descriptorKey.getUniversalKey());
        Collection<String> roleNames = getCurrentUserRoleNames();
        return roleNames.stream()
                   .filter(this::isAlertRole)
                   .anyMatch(name -> permissionCache.containsKey(name) && permissionCache.get(name).hasPermissions(permissionKey, operations));
    }

    public void updateRoleName(Long roleId, String roleName) throws AlertForbiddenOperationException {
        roleAccessor.updateRoleName(roleId, roleName);
        loadPermissionsIntoCache();
    }

    public UserRoleModel createRoleWithPermissions(String roleName, PermissionMatrixModel permissionMatrix) {
        UserRoleModel roleWithPermissions = roleAccessor.createRoleWithPermissions(roleName, permissionMatrix);
        updateRoleInCache(roleWithPermissions.getName(), roleWithPermissions.getPermissions());
        return roleWithPermissions;
    }

    public PermissionMatrixModel updatePermissionsForRole(String roleName, PermissionMatrixModel permissionMatrix) throws AlertConfigurationException {
        PermissionMatrixModel permissionMatrixModel = roleAccessor.updatePermissionsForRole(roleName, permissionMatrix);
        updateRoleInCache(roleName, permissionMatrixModel);
        return permissionMatrixModel;
    }

    public void deleteRole(Long roleId) throws AlertForbiddenOperationException {
        roleAccessor.deleteRole(roleId);
        loadPermissionsIntoCache();
    }

    public void updateUserRoles(Long userId, Collection<UserRoleModel> roles) {
        roleAccessor.updateUserRoles(userId, roles);
        roles.stream()
            .forEach(userRoleModel -> updateRoleInCache(userRoleModel.getName(), userRoleModel.getPermissions()));
    }

    private boolean isAlertRole(String roleName) {
        return roleAccessor.doesRoleNameExist(roleName);
    }

    private boolean currentUserAnyPermission(AccessOperation operation, Collection<PermissionKey> permissionKeys) {
        Collection<String> roleNames = getCurrentUserRoleNames();
        return roleNames.stream()
                   .anyMatch(name -> permissionCache.containsKey(name) && permissionCache.get(name).anyPermissionMatch(operation, permissionKeys));
    }

    private boolean currentUserHasPermission(AccessOperation operation, ConfigContextEnum context, DescriptorKey descriptorKey) {
        return currentUserHasPermission(operation, context.name(), descriptorKey.getUniversalKey());
    }

    private boolean currentUserHasPermission(AccessOperation operation, String context, String descriptorKey) {
        PermissionKey permissionKey = new PermissionKey(context, descriptorKey);
        Collection<String> roleNames = getCurrentUserRoleNames();
        boolean hasPermission = roleNames.stream()
                                    .anyMatch(name -> permissionCache.containsKey(name) && permissionCache.get(name).hasPermission(permissionKey, operation));
        if (!hasPermission) {
            logger.debug(String.format("User %s does not have permission: %s", getCurrentUserName().orElse("UNKNOWN"), operation.name()));
        }
        return hasPermission;
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

    private Optional<String> getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null == authentication || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Optional.of(userDetails.getUsername());
    }

    private void loadPermissionsIntoCache() {
        Collection<UserRoleModel> roles = roleAccessor.getRoles();
        permissionCache.putAll(roles.stream().collect(Collectors.toMap(UserRoleModel::getName, UserRoleModel::getPermissions)));
    }

    private void updateRoleInCache(String roleName, PermissionMatrixModel permissions) {
        permissionCache.put(roleName, permissions);
    }

}
