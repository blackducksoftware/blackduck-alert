/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.descriptor.accessor;

import java.util.Collection;
import java.util.Set;

import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.common.exception.AlertForbiddenOperationException;
import com.blackduck.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.blackduck.integration.alert.common.persistence.model.UserRoleModel;

public interface RoleAccessor {
    Set<UserRoleModel> getRoles();

    Set<UserRoleModel> getRoles(Collection<Long> roleIds);

    boolean doesRoleNameExist(String name);

    UserRoleModel createRoleWithPermissions(String roleName, PermissionMatrixModel permissionMatrix);

    void updateRoleName(Long roleId, String roleName) throws AlertException;

    PermissionMatrixModel updatePermissionsForRole(String roleName, PermissionMatrixModel permissionMatrix) throws AlertConfigurationException;

    void deleteRole(Long roleId) throws AlertForbiddenOperationException;

    PermissionMatrixModel mergePermissionsForRoles(Collection<String> roleNames);

    PermissionMatrixModel readPermissionsForRole(Long roleId);

    void updateUserRoles(Long userId, Collection<UserRoleModel> roles);

}
