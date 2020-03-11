package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.enumeration.DefaultUserRole;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.database.authorization.PermissionMatrixRelation;
import com.synopsys.integration.alert.database.authorization.PermissionMatrixRepository;
import com.synopsys.integration.alert.database.configuration.ConfigContextEntity;
import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.database.user.RoleEntity;
import com.synopsys.integration.alert.database.user.RoleRepository;
import com.synopsys.integration.alert.database.user.UserRoleRepository;

public class DefaultAuthorizationUtilityTest {
    @Test
    public void testSuperSetRoles() {
        RoleRepository roleRepository = Mockito.mock(RoleRepository.class);
        UserRoleRepository userRoleRepository = Mockito.mock(UserRoleRepository.class);
        PermissionMatrixRepository permissionMatrixRepository = Mockito.mock(PermissionMatrixRepository.class);
        RegisteredDescriptorRepository registeredDescriptorRepository = Mockito.mock(RegisteredDescriptorRepository.class);
        ConfigContextRepository configContextRepository = Mockito.mock(ConfigContextRepository.class);

        RoleEntity adminRole = new RoleEntity(DefaultUserRole.ALERT_ADMIN.name(), true);
        adminRole.setId(1L);
        RoleEntity userRole = new RoleEntity(DefaultUserRole.ALERT_USER.name(), true);
        userRole.setId(2L);

        Mockito.when(roleRepository.findRoleEntitiesByRoleNames(Mockito.anyCollection())).thenReturn(List.of(adminRole, userRole));

        Long contextId = 1L;
        String contextString = "PERMISSION";
        ConfigContextEntity contextEntity = new ConfigContextEntity(contextString);
        contextEntity.setId(contextId);
        Mockito.when(configContextRepository.findById(Mockito.eq(contextEntity.getId()))).thenReturn(Optional.of(contextEntity));

        Long descriptorId_1 = 1L;
        String descriptorName_1 = "key.1";
        Long descriptorId_2 = 2L;
        String descriptorName_2 = "key.2";
        Long descriptorId_3 = 3L;
        String descriptorName_3 = "key.3";

        RegisteredDescriptorEntity registeredDescriptorEntity_1 = new RegisteredDescriptorEntity(descriptorName_1, 1L);
        registeredDescriptorEntity_1.setId(descriptorId_1);
        Mockito.when(registeredDescriptorRepository.findById(Mockito.eq(registeredDescriptorEntity_1.getId()))).thenReturn(Optional.of(registeredDescriptorEntity_1));

        RegisteredDescriptorEntity registeredDescriptorEntity_2 = new RegisteredDescriptorEntity(descriptorName_2, 1L);
        registeredDescriptorEntity_2.setId(descriptorId_2);
        Mockito.when(registeredDescriptorRepository.findById(Mockito.eq(registeredDescriptorEntity_2.getId()))).thenReturn(Optional.of(registeredDescriptorEntity_2));

        RegisteredDescriptorEntity registeredDescriptorEntity_3 = new RegisteredDescriptorEntity(descriptorName_3, 1L);
        registeredDescriptorEntity_3.setId(descriptorId_3);
        Mockito.when(registeredDescriptorRepository.findById(Mockito.eq(registeredDescriptorEntity_3.getId()))).thenReturn(Optional.of(registeredDescriptorEntity_3));

        PermissionKey permission_1 = new PermissionKey(contextString, descriptorName_1);
        PermissionKey permission_2 = new PermissionKey(contextString, descriptorName_2);
        PermissionKey permission_3 = new PermissionKey(contextString, descriptorName_3);

        PermissionMatrixRelation adminRelation_1 = new PermissionMatrixRelation(adminRole.getId(), contextEntity.getId(), registeredDescriptorEntity_1.getId(), AccessOperation.READ.getBit() + AccessOperation.WRITE.getBit());
        PermissionMatrixRelation adminRelation_3 = new PermissionMatrixRelation(adminRole.getId(), contextEntity.getId(), registeredDescriptorEntity_3.getId(), AccessOperation.READ.getBit() + AccessOperation.WRITE.getBit());
        PermissionMatrixRelation userRelation_1 = new PermissionMatrixRelation(userRole.getId(), contextEntity.getId(), registeredDescriptorEntity_1.getId(), AccessOperation.READ.getBit());
        PermissionMatrixRelation userRelation_2 = new PermissionMatrixRelation(userRole.getId(), contextEntity.getId(), registeredDescriptorEntity_2.getId(), AccessOperation.READ.getBit() + AccessOperation.EXECUTE.getBit());
        List<Long> roleIds = List.of(adminRole.getId(), userRole.getId());
        Mockito.when(permissionMatrixRepository.findAllByRoleId(Mockito.eq(adminRole.getId()))).thenReturn(List.of(adminRelation_1, adminRelation_3));
        Mockito.when(permissionMatrixRepository.findAllByRoleId(Mockito.eq(userRole.getId()))).thenReturn(List.of(userRelation_1, userRelation_2));
        Mockito.when(permissionMatrixRepository.findAllByRoleIdIn(Mockito.eq(roleIds))).thenReturn(List.of(adminRelation_1, adminRelation_3, userRelation_1, userRelation_2));

        DefaultAuthorizationUtility authorizationUtility =
            new DefaultAuthorizationUtility(roleRepository, userRoleRepository, permissionMatrixRepository, registeredDescriptorRepository, configContextRepository);

        // order matters here.  The userRole has less privileges so we want to test that the more restrictive privileges don't overwrite the admin privileges.  We want a union of the permissions
        List<String> roles = List.of(adminRole.getRoleName(), userRole.getRoleName());

        PermissionMatrixModel matrixModel = authorizationUtility.mergePermissionsForRoles(roles);

        // admin read/write
        assertTrue(matrixModel.hasPermission(permission_1, AccessOperation.READ));
        assertTrue(matrixModel.hasPermission(permission_1, AccessOperation.WRITE));
        assertFalse(matrixModel.hasPermission(permission_1, AccessOperation.EXECUTE));

        // user read/execute
        assertTrue(matrixModel.hasPermission(permission_2, AccessOperation.READ));
        assertFalse(matrixModel.hasPermission(permission_2, AccessOperation.WRITE));
        assertTrue(matrixModel.hasPermission(permission_2, AccessOperation.EXECUTE));

        // admin read/write
        assertTrue(matrixModel.hasPermission(permission_3, AccessOperation.READ));
        assertTrue(matrixModel.hasPermission(permission_3, AccessOperation.WRITE));
        assertFalse(matrixModel.hasPermission(permission_3, AccessOperation.EXECUTE));
    }

}
