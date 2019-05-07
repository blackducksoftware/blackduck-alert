package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.enumeration.UserRole;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.database.authorization.AccessOperationEntity;
import com.synopsys.integration.alert.database.authorization.AccessOperationRepository;
import com.synopsys.integration.alert.database.authorization.PermissionKeyEntity;
import com.synopsys.integration.alert.database.authorization.PermissionKeyRepository;
import com.synopsys.integration.alert.database.authorization.PermissionMatrixRelation;
import com.synopsys.integration.alert.database.authorization.PermissionMatrixRepository;
import com.synopsys.integration.alert.database.user.RoleEntity;
import com.synopsys.integration.alert.database.user.RoleRepository;
import com.synopsys.integration.alert.database.user.UserRoleRepository;

public class AuthorizationUtilityTest {

    @Test
    public void testSuperSetRoles() {
        final RoleRepository roleRepository = Mockito.mock(RoleRepository.class);
        final UserRoleRepository userRoleRepository = Mockito.mock(UserRoleRepository.class);
        final PermissionMatrixRepository permissionMatrixRepository = Mockito.mock(PermissionMatrixRepository.class);
        final AccessOperationRepository accessOperationRepository = Mockito.mock(AccessOperationRepository.class);
        final PermissionKeyRepository permissionKeyRepository = Mockito.mock(PermissionKeyRepository.class);

        final RoleEntity adminRole = new RoleEntity(UserRole.ALERT_ADMIN_TEXT);
        adminRole.setId(1L);
        final RoleEntity userRole = new RoleEntity(UserRole.ALERT_USER_TEXT);
        userRole.setId(2L);

        Mockito.when(roleRepository.findRoleEntitiesByRoleName(Mockito.anyCollection())).thenReturn(List.of(adminRole, userRole));

        final String permissionKey_1 = "permission.key.1";
        final String permissionKey_2 = "permission.key.2";
        final String permissionKey_3 = "permission.key.3";
        final PermissionKeyEntity permission_1 = new PermissionKeyEntity(permissionKey_1);
        permission_1.setId(1L);
        final PermissionKeyEntity permission_2 = new PermissionKeyEntity(permissionKey_2);
        permission_2.setId(2L);
        final PermissionKeyEntity permission_3 = new PermissionKeyEntity(permissionKey_3);
        permission_3.setId(3L);
        final AccessOperationEntity access_read = new AccessOperationEntity(AccessOperation.READ.name());
        access_read.setId(Long.valueOf(AccessOperation.READ.ordinal()));
        final AccessOperationEntity access_write = new AccessOperationEntity(AccessOperation.WRITE.name());
        access_write.setId(Long.valueOf(AccessOperation.WRITE.ordinal()));
        final AccessOperationEntity access_execute = new AccessOperationEntity(AccessOperation.EXECUTE.name());
        access_execute.setId(Long.valueOf(AccessOperation.EXECUTE.ordinal()));

        final PermissionMatrixRelation adminRelation_1 = new PermissionMatrixRelation(adminRole.getId(), permission_1.getId(), access_read.getId());
        final PermissionMatrixRelation adminRelation_2 = new PermissionMatrixRelation(adminRole.getId(), permission_1.getId(), access_write.getId());
        final PermissionMatrixRelation adminRelation_3 = new PermissionMatrixRelation(adminRole.getId(), permission_3.getId(), access_read.getId());
        final PermissionMatrixRelation adminRelation_4 = new PermissionMatrixRelation(adminRole.getId(), permission_3.getId(), access_write.getId());
        final PermissionMatrixRelation userRelation_1 = new PermissionMatrixRelation(userRole.getId(), permission_1.getId(), access_read.getId());
        final PermissionMatrixRelation userRelation_2 = new PermissionMatrixRelation(userRole.getId(), permission_2.getId(), access_read.getId());
        final PermissionMatrixRelation userRelation_3 = new PermissionMatrixRelation(userRole.getId(), permission_2.getId(), access_execute.getId());
        final List<Long> roleIds = List.of(adminRole.getId(), userRole.getId());
        Mockito.when(permissionMatrixRepository.findAllByRoleId(Mockito.eq(adminRole.getId()))).thenReturn(List.of(adminRelation_1, adminRelation_2, adminRelation_3, adminRelation_4));
        Mockito.when(permissionMatrixRepository.findAllByRoleId(Mockito.eq(userRole.getId()))).thenReturn(List.of(userRelation_1, userRelation_2, userRelation_3));
        Mockito.when(permissionMatrixRepository.findAllByRoleIdIn(Mockito.eq(roleIds))).thenReturn(List.of(adminRelation_1, adminRelation_2, adminRelation_3, adminRelation_4, userRelation_1, userRelation_2, userRelation_3));

        Mockito.when(permissionKeyRepository.findById(Mockito.eq(permission_1.getId()))).thenReturn(Optional.of(permission_1));
        Mockito.when(permissionKeyRepository.findById(Mockito.eq(permission_2.getId()))).thenReturn(Optional.of(permission_2));
        Mockito.when(permissionKeyRepository.findById(Mockito.eq(permission_3.getId()))).thenReturn(Optional.of(permission_3));

        Mockito.when(accessOperationRepository.findById(Mockito.eq(access_read.getId()))).thenReturn(Optional.of(access_read));
        Mockito.when(accessOperationRepository.findById(Mockito.eq(access_write.getId()))).thenReturn(Optional.of(access_write));
        Mockito.when(accessOperationRepository.findById(Mockito.eq(access_execute.getId()))).thenReturn(Optional.of(access_execute));

        final DefaultAuthorizationUtility authorizationUtility = new DefaultAuthorizationUtility(roleRepository, userRoleRepository, permissionMatrixRepository, accessOperationRepository, permissionKeyRepository);

        // order matters here.  The userRole has less privileges so we want to test that the more restrictive privileges don't overwrite the admin privileges.  We want a union of the permissions
        final List<String> roles = List.of(adminRole.getRoleName(), userRole.getRoleName());

        final PermissionMatrixModel matrixModel = authorizationUtility.mergePermissionsForRoles(roles);

        // admin read/write
        assertTrue(matrixModel.hasPermission(permissionKey_1, AccessOperation.READ));
        assertTrue(matrixModel.hasPermission(permissionKey_1, AccessOperation.WRITE));
        assertFalse(matrixModel.hasPermission(permissionKey_1, AccessOperation.EXECUTE));

        // user read/execute
        assertTrue(matrixModel.hasPermission(permissionKey_2, AccessOperation.READ));
        assertFalse(matrixModel.hasPermission(permissionKey_2, AccessOperation.WRITE));
        assertTrue(matrixModel.hasPermission(permissionKey_2, AccessOperation.EXECUTE));

        // admin read/write
        assertTrue(matrixModel.hasPermission(permissionKey_3, AccessOperation.READ));
        assertTrue(matrixModel.hasPermission(permissionKey_3, AccessOperation.WRITE));
        assertFalse(matrixModel.hasPermission(permissionKey_3, AccessOperation.EXECUTE));

    }
}
