package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Collection;
import java.util.Set;

import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;

public interface AuthorizationUtil {
    Set<UserRoleModel> createRoleModels(final Collection<Long> roleIds);

    PermissionMatrixModel readPermissionsForRole(final String roleName);

    PermissionMatrixModel readPermissionsForRole(final Long roleId);

    void updateUserRoles(final Long userId, final Collection<UserRoleModel> roles);

}
