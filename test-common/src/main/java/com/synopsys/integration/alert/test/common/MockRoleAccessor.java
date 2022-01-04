/*
 * test-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.test.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.exception.AlertForbiddenOperationException;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;

public class MockRoleAccessor implements RoleAccessor {
    private AtomicLong latestId = new AtomicLong(0);
    private Map<Long, UserRoleModel> roleMap = new HashMap<>();

    @Override
    public Set<UserRoleModel> getRoles() {
        return roleMap.entrySet().stream()
            .map(Map.Entry::getValue)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<UserRoleModel> getRoles(Collection<Long> roleIds) {
        return roleMap.entrySet().stream()
            .filter(entry -> roleIds.contains(entry.getKey()))
            .map(Map.Entry::getValue)
            .collect(Collectors.toSet());
    }

    @Override
    public boolean doesRoleNameExist(String name) {
        return roleMap.values().stream()
            .map(UserRoleModel::getName)
            .anyMatch(roleName -> roleName.equals(name));
    }

    @Override
    public UserRoleModel createRoleWithPermissions(String roleName, PermissionMatrixModel permissionMatrix) {
        Long id = latestId.incrementAndGet();
        return roleMap.computeIfAbsent(id, (ignoredId) -> new UserRoleModel(id, roleName, true, permissionMatrix));
    }

    @Override
    public void updateRoleName(Long roleId, String roleName) throws AlertForbiddenOperationException {
        roleMap.computeIfPresent(roleId, (ignored, role) -> new UserRoleModel(role.getId(), role.getName(), role.isCustom(), role.getPermissions()));
    }

    @Override
    public PermissionMatrixModel updatePermissionsForRole(String roleName, PermissionMatrixModel permissionMatrix) throws AlertConfigurationException {
        Long roleId = roleMap.entrySet().stream()
            .filter(entry -> entry.getValue().getName().equals(roleName))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElseThrow(() -> new AlertConfigurationException(String.format("role with name %s not found", roleName)));

        roleMap.computeIfPresent(roleId, (ignored, role) -> new UserRoleModel(role.getId(), role.getName(), role.isCustom(), permissionMatrix));
        return permissionMatrix;
    }

    @Override
    public void deleteRole(Long roleId) throws AlertForbiddenOperationException {
        roleMap.remove(roleId);
    }

    @Override
    public PermissionMatrixModel mergePermissionsForRoles(Collection<String> roleNames) {
        throw new UnsupportedOperationException("This method is not implemented by this class");
    }

    @Override
    public PermissionMatrixModel readPermissionsForRole(Long roleId) {
        return roleMap.get(roleId).getPermissions();
    }

    @Override
    public void updateUserRoles(Long userId, Collection<UserRoleModel> roles) {
        throw new UnsupportedOperationException("This method is not implemented by this class");
    }
}
