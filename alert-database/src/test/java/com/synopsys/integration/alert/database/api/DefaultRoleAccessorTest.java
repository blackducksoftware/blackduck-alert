package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.enumeration.DefaultUserRole;
import com.synopsys.integration.alert.common.exception.AlertForbiddenOperationException;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.database.authorization.PermissionMatrixRelation;
import com.synopsys.integration.alert.database.authorization.PermissionMatrixRepository;
import com.synopsys.integration.alert.database.configuration.ConfigContextEntity;
import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.database.user.RoleEntity;
import com.synopsys.integration.alert.database.user.RoleRepository;
import com.synopsys.integration.alert.database.user.UserRoleRepository;

public class DefaultRoleAccessorTest {
    private RoleRepository roleRepository;
    private UserRoleRepository userRoleRepository;
    private PermissionMatrixRepository permissionMatrixRepository;
    private RegisteredDescriptorRepository registeredDescriptorRepository;
    private ConfigContextRepository configContextRepository;

    @BeforeEach
    public void init() {
        this.roleRepository = Mockito.mock(RoleRepository.class);
        this.userRoleRepository = Mockito.mock(UserRoleRepository.class);
        this.permissionMatrixRepository = Mockito.mock(PermissionMatrixRepository.class);
        this.registeredDescriptorRepository = Mockito.mock(RegisteredDescriptorRepository.class);
        this.configContextRepository = Mockito.mock(ConfigContextRepository.class);
    }

    @Test
    public void getRolesTest() {
        RoleEntity roleEntity = new RoleEntity(DefaultUserRole.ALERT_USER.name(), true);
        roleEntity.setId(1L);

        Mockito.when(roleRepository.findAll()).thenReturn(List.of(roleEntity));

        DefaultRoleAccessor authorizationUtility = new DefaultRoleAccessor(roleRepository, userRoleRepository, permissionMatrixRepository, registeredDescriptorRepository, configContextRepository);
        Set<UserRoleModel> userRoleModelsSet = authorizationUtility.getRoles();

        UserRoleModel expectedUserRoleModel = createUserRoleModel(1L, DefaultUserRole.ALERT_USER.name(), true);

        assertEquals(1, userRoleModelsSet.size());
        assertTrue(userRoleModelsSet.contains(expectedUserRoleModel));
    }

    @Test
    public void getRolesByRoleIdsTest() {
        RoleEntity roleEntity = new RoleEntity(DefaultUserRole.ALERT_USER.name(), true);
        roleEntity.setId(1L);

        Mockito.when(roleRepository.findById(Mockito.any())).thenReturn(Optional.of(roleEntity));

        DefaultRoleAccessor authorizationUtility = new DefaultRoleAccessor(roleRepository, userRoleRepository, permissionMatrixRepository, registeredDescriptorRepository, configContextRepository);
        Set<UserRoleModel> userRoleModelsSet = authorizationUtility.getRoles(List.of(1L));

        UserRoleModel expectedUserRoleModel = createUserRoleModel(1L, DefaultUserRole.ALERT_USER.name(), true);

        assertEquals(1, userRoleModelsSet.size());
        assertTrue(userRoleModelsSet.contains(expectedUserRoleModel));
    }

    @Test
    public void doesRoleNameExistTest() {
        Mockito.when(roleRepository.existsRoleEntityByRoleName(Mockito.any())).thenReturn(true);
        DefaultRoleAccessor authorizationUtility = new DefaultRoleAccessor(roleRepository, userRoleRepository, permissionMatrixRepository, registeredDescriptorRepository, configContextRepository);

        assertTrue(authorizationUtility.doesRoleNameExist("name"));
    }

    @Test
    public void createRoleWithPermissions() {
        final String roleName = "roleName";
        final String contextString = "context-test";
        final String descriptorName = "descriptorName";

        RoleEntity roleEntity = new RoleEntity(roleName, true);
        roleEntity.setId(1L);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(contextString);
        configContextEntity.setId(1L);
        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity(descriptorName, 1L);
        registeredDescriptorEntity.setId(1L);

        PermissionKey permissionKey = new PermissionKey(contextString, descriptorName);
        PermissionMatrixModel permissionMatrixModel = new PermissionMatrixModel(Map.of(permissionKey, AccessOperation.READ.getBit() + AccessOperation.WRITE.getBit()));
        PermissionMatrixRelation permissionMatrixRelation = new PermissionMatrixRelation(roleEntity.getId(), configContextEntity.getId(), registeredDescriptorEntity.getId(), AccessOperation.READ.getBit() + AccessOperation.WRITE.getBit());

        Mockito.when(roleRepository.save(Mockito.any())).thenReturn(new RoleEntity(roleName, true));
        mockUpdateRoleOperations(permissionMatrixRelation, configContextEntity, registeredDescriptorEntity);
        mockCreateModelFromPermission(configContextEntity, registeredDescriptorEntity);

        DefaultRoleAccessor authorizationUtility = new DefaultRoleAccessor(roleRepository, userRoleRepository, permissionMatrixRepository, registeredDescriptorRepository, configContextRepository);
        UserRoleModel userRoleModel = authorizationUtility.createRoleWithPermissions(roleName, permissionMatrixModel);

        Mockito.verify(permissionMatrixRepository).deleteAll(Mockito.any());

        assertEquals(roleName, userRoleModel.getName());
        assertTrue(userRoleModel.isCustom());
        assertEquals(permissionMatrixModel, userRoleModel.getPermissions());
    }

    @Test
    public void updateRoleNameTest() throws Exception {
        final String roleName = "roleName";
        final Long roleId = 1L;

        RoleEntity roleEntity = new RoleEntity(DefaultUserRole.ALERT_USER.name(), true);
        roleEntity.setId(1L);

        Mockito.when(roleRepository.findById(Mockito.any())).thenReturn(Optional.of(roleEntity));

        DefaultRoleAccessor authorizationUtility = new DefaultRoleAccessor(roleRepository, userRoleRepository, permissionMatrixRepository, registeredDescriptorRepository, configContextRepository);
        authorizationUtility.updateRoleName(roleId, roleName);

        Mockito.verify(roleRepository).save(Mockito.any());
    }

    @Test
    public void updatePermissionsForRole() throws Exception {
        final String roleName = "roleName";
        final String contextString = "context-test";
        final String descriptorName = "descriptorName";

        RoleEntity roleEntity = new RoleEntity(roleName, true);
        roleEntity.setId(1L);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(contextString);
        configContextEntity.setId(1L);
        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity(descriptorName, 1L);
        registeredDescriptorEntity.setId(1L);

        PermissionKey permissionKey = new PermissionKey(contextString, descriptorName);
        PermissionMatrixModel permissionMatrix = new PermissionMatrixModel(Map.of(permissionKey, AccessOperation.READ.getBit() + AccessOperation.WRITE.getBit()));
        PermissionMatrixRelation permissionMatrixRelation = new PermissionMatrixRelation(roleEntity.getId(), configContextEntity.getId(), registeredDescriptorEntity.getId(), AccessOperation.READ.getBit() + AccessOperation.WRITE.getBit());

        Mockito.when(roleRepository.findByRoleName(Mockito.any())).thenReturn(Optional.of(roleEntity));
        mockUpdateRoleOperations(permissionMatrixRelation, configContextEntity, registeredDescriptorEntity);
        mockCreateModelFromPermission(configContextEntity, registeredDescriptorEntity);

        DefaultRoleAccessor authorizationUtility = new DefaultRoleAccessor(roleRepository, userRoleRepository, permissionMatrixRepository, registeredDescriptorRepository, configContextRepository);
        PermissionMatrixModel permissionMatrixModel = authorizationUtility.updatePermissionsForRole(roleName, permissionMatrix);

        Mockito.verify(permissionMatrixRepository).saveAll(Mockito.any());

        assertFalse(permissionMatrixModel.isEmpty());
        assertEquals(permissionMatrix, permissionMatrixModel);
    }

    @Test
    public void deleteRoleTest() throws Exception {
        final String roleName = "roleName";
        final Long roleId = 1L;

        RoleEntity roleEntity = new RoleEntity(roleName, true);
        roleEntity.setId(1L);

        Mockito.when(roleRepository.findById(Mockito.any())).thenReturn(Optional.of(roleEntity));

        DefaultRoleAccessor authorizationUtility = new DefaultRoleAccessor(roleRepository, userRoleRepository, permissionMatrixRepository, registeredDescriptorRepository, configContextRepository);
        authorizationUtility.deleteRole(roleId);

        Mockito.verify(roleRepository).deleteById(Mockito.any());
    }

    @Test
    public void deleteRoleCustomFalseTest() {
        DefaultRoleAccessor authorizationUtility = new DefaultRoleAccessor(roleRepository, userRoleRepository, permissionMatrixRepository, registeredDescriptorRepository, configContextRepository);

        RoleEntity roleEntity = new RoleEntity("name", false);
        roleEntity.setId(1L);
        Mockito.when(roleRepository.findById(Mockito.any())).thenReturn(Optional.of(roleEntity));

        try {
            authorizationUtility.deleteRole(1L);
            fail("Custom parameter of roleEntity set to 'false' did not throw expected AlertForbiddenOperationException.");
        } catch (AlertForbiddenOperationException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void updateUserRolesTest() {
        final Long userId = 1L;
        final String roleName = "roleName";
        final Long roleId = 1L;

        RoleEntity roleEntity = new RoleEntity(roleName, true);
        roleEntity.setId(1L);
        UserRoleModel userRoleModel = createUserRoleModel(roleId, roleName, true);
        Collection<UserRoleModel> userRoleModelCollection = List.of(userRoleModel);

        Mockito.when(roleRepository.findRoleEntitiesByRoleNames(Mockito.any())).thenReturn(List.of(roleEntity));

        DefaultRoleAccessor authorizationUtility = new DefaultRoleAccessor(roleRepository, userRoleRepository, permissionMatrixRepository, registeredDescriptorRepository, configContextRepository);
        authorizationUtility.updateUserRoles(userId, userRoleModelCollection);

        Mockito.verify(userRoleRepository).bulkDeleteAllByUserId(Mockito.any());
        Mockito.verify(userRoleRepository).saveAll(Mockito.any());
    }

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

        DefaultRoleAccessor authorizationUtility =
            new DefaultRoleAccessor(roleRepository, userRoleRepository, permissionMatrixRepository, registeredDescriptorRepository, configContextRepository);

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

    private void mockUpdateRoleOperations(PermissionMatrixRelation permissionMatrixRelation, ConfigContextEntity configContextEntity, RegisteredDescriptorEntity registeredDescriptorEntity) {
        Mockito.when(permissionMatrixRepository.findAllByRoleId(Mockito.any())).thenReturn(List.of(permissionMatrixRelation));
        Mockito.when(configContextRepository.findFirstByContext(Mockito.any())).thenReturn(Optional.of(configContextEntity));
        Mockito.when(registeredDescriptorRepository.findFirstByName(Mockito.any())).thenReturn(Optional.of(registeredDescriptorEntity));
        Mockito.when(permissionMatrixRepository.saveAll(Mockito.any())).thenReturn(List.of(permissionMatrixRelation));
    }

    private void mockCreateModelFromPermission(ConfigContextEntity configContextEntity, RegisteredDescriptorEntity registeredDescriptorEntity) {
        Mockito.when(configContextRepository.findById(Mockito.any())).thenReturn(Optional.of(configContextEntity));
        Mockito.when(registeredDescriptorRepository.findById(Mockito.any())).thenReturn(Optional.of(registeredDescriptorEntity));
    }

    private UserRoleModel createUserRoleModel(Long id, String name, Boolean custom) {
        PermissionMatrixModel permissionMatrixModel = new PermissionMatrixModel(Map.of());
        return new UserRoleModel(id, name, custom, permissionMatrixModel);
    }

}
