/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.accessor;

import java.util.Collection;
import java.util.Set;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertForbiddenOperationException;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;

public interface RoleAccessor {
    Set<UserRoleModel> getRoles();

    Set<UserRoleModel> getRoles(Collection<Long> roleIds);

    boolean doesRoleNameExist(String name);

    UserRoleModel createRole(String roleName) throws AlertDatabaseConstraintException;

    UserRoleModel createRoleWithPermissions(String roleName, PermissionMatrixModel permissionMatrix) throws AlertDatabaseConstraintException;

    void updateRoleName(Long roleId, String roleName) throws AlertDatabaseConstraintException;

    PermissionMatrixModel updatePermissionsForRole(String roleName, PermissionMatrixModel permissionMatrix) throws AlertDatabaseConstraintException;

    void deleteRole(Long roleId) throws AlertForbiddenOperationException;

    PermissionMatrixModel mergePermissionsForRoles(Collection<String> roleNames);

    PermissionMatrixModel readPermissionsForRole(Long roleId);

    void updateUserRoles(Long userId, Collection<UserRoleModel> roles);

}
