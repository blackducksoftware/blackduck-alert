package com.synopsys.integration.alert.web.api.role;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertForbiddenOperationException;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.users.UserManagementDescriptorKey;
import com.synopsys.integration.alert.web.api.role.util.PermissionModelUtil;

public class RoleActionsTest {
    private final String id = "1L";
    private final String roleName = "roleName";
    private final String context = "GLOBAL";

    private UserManagementDescriptorKey userManagementDescriptorKey;
    private AuthorizationManager authorizationManager;
    private DescriptorMap descriptorMap;
    private DescriptorKey descriptorKey;
    private RoleAccessor roleAccessor;

    @BeforeEach
    public void init() {
        userManagementDescriptorKey = Mockito.mock(UserManagementDescriptorKey.class);
        authorizationManager = Mockito.mock(AuthorizationManager.class);
        descriptorMap = Mockito.mock(DescriptorMap.class);
        roleAccessor = Mockito.mock(RoleAccessor.class);
        descriptorKey = createDescriptorKey("descriptorKey-test");
    }

    @Test
    public void createWithoutChecksTest() throws Exception {
        PermissionModel permissionModel = createPermissionModel();
        RolePermissionModel rolePermissionModel = new RolePermissionModel(id, roleName, Set.of(permissionModel));
        UserRoleModel userRoleModel = new UserRoleModel(1L, roleName, false, PermissionModelUtil.convertToPermissionMatrixModel(Set.of(permissionModel)));

        Mockito.when(authorizationManager.createRoleWithPermissions(Mockito.eq(roleName), Mockito.any())).thenReturn(userRoleModel);

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap, List.of(descriptorKey));

        ActionResponse<RolePermissionModel> rolePermissionModelActionResponse = roleActions.createWithoutChecks(rolePermissionModel);

        assertTrue(rolePermissionModelActionResponse.isSuccessful());
        assertTrue(rolePermissionModelActionResponse.hasContent());
    }

    @Test
    public void createWithoutChecksErrorTest() throws Exception {
        PermissionModel permissionModel = createPermissionModel();
        RolePermissionModel rolePermissionModel = new RolePermissionModel(id, roleName, Set.of(permissionModel));

        Mockito.when(authorizationManager.createRoleWithPermissions(Mockito.eq(roleName), Mockito.any())).thenThrow(new AlertDatabaseConstraintException("Exception for test"));

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap, List.of(descriptorKey));

        ActionResponse<RolePermissionModel> rolePermissionModelActionResponse = roleActions.createWithoutChecks(rolePermissionModel);

        assertTrue(rolePermissionModelActionResponse.isError());
        assertFalse(rolePermissionModelActionResponse.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, rolePermissionModelActionResponse.getHttpStatus());
    }

    @Test
    public void deleteWithoutChecksTest() throws Exception {
        PermissionModel permissionModel = createPermissionModel();
        UserRoleModel userRoleModel = new UserRoleModel(1L, roleName, false, PermissionModelUtil.convertToPermissionMatrixModel(Set.of(permissionModel)));

        Mockito.when(roleAccessor.getRoles(Mockito.anyCollection())).thenReturn(Set.of(userRoleModel));

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap, List.of(descriptorKey));
        ActionResponse<RolePermissionModel> rolePermissionModelActionResponse = roleActions.deleteWithoutChecks(1L);

        Mockito.verify(authorizationManager).deleteRole(Mockito.anyLong());

        assertTrue(rolePermissionModelActionResponse.isSuccessful());
        assertFalse(rolePermissionModelActionResponse.hasContent());
        assertEquals(HttpStatus.NO_CONTENT, rolePermissionModelActionResponse.getHttpStatus());
    }

    @Test
    public void deleteWithoutChecksErrorTest() throws Exception {
        PermissionModel permissionModel = createPermissionModel();
        UserRoleModel userRoleModel = new UserRoleModel(1L, roleName, false, PermissionModelUtil.convertToPermissionMatrixModel(Set.of(permissionModel)));

        Mockito.when(roleAccessor.getRoles(Mockito.anyCollection())).thenReturn(Set.of(userRoleModel));
        doThrow(new AlertForbiddenOperationException("Exception for test")).when(authorizationManager).deleteRole(Mockito.anyLong());

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap, List.of(descriptorKey));
        ActionResponse<RolePermissionModel> rolePermissionModelActionResponse = roleActions.deleteWithoutChecks(1L);

        assertTrue(rolePermissionModelActionResponse.isError());
        assertFalse(rolePermissionModelActionResponse.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, rolePermissionModelActionResponse.getHttpStatus());
    }

    @Test
    public void deleteWithoutChecksNotFoundTest() {
        Mockito.when(roleAccessor.getRoles(Mockito.anyCollection())).thenReturn(Set.of());

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap, List.of(descriptorKey));
        ActionResponse<RolePermissionModel> rolePermissionModelActionResponse = roleActions.deleteWithoutChecks(1L);

        assertTrue(rolePermissionModelActionResponse.isError());
        assertFalse(rolePermissionModelActionResponse.hasContent());
        assertEquals(HttpStatus.NOT_FOUND, rolePermissionModelActionResponse.getHttpStatus());
    }

    @Test
    public void readAllWithoutChecksTest() {
        PermissionModel permissionModel = createPermissionModel();
        UserRoleModel userRoleModel = new UserRoleModel(1L, roleName, false, PermissionModelUtil.convertToPermissionMatrixModel(Set.of(permissionModel)));

        Mockito.when(roleAccessor.getRoles()).thenReturn(Set.of(userRoleModel));

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap, List.of(descriptorKey));
        ActionResponse<MultiRolePermissionModel> rolePermissionModelActionResponse = roleActions.readAllWithoutChecks();

        assertTrue(rolePermissionModelActionResponse.isSuccessful());
        assertTrue(rolePermissionModelActionResponse.hasContent());
        assertEquals(HttpStatus.OK, rolePermissionModelActionResponse.getHttpStatus());
    }

    @Test
    public void readWithoutChecksTest() {
        PermissionModel permissionModel = createPermissionModel();
        UserRoleModel userRoleModel = new UserRoleModel(1L, roleName, false, PermissionModelUtil.convertToPermissionMatrixModel(Set.of(permissionModel)));

        Mockito.when(roleAccessor.getRoles(Mockito.anyCollection())).thenReturn(Set.of(userRoleModel));

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap, List.of(descriptorKey));
        ActionResponse<RolePermissionModel> rolePermissionModelActionResponse = roleActions.readWithoutChecks(1L);

        assertTrue(rolePermissionModelActionResponse.isSuccessful());
        assertTrue(rolePermissionModelActionResponse.hasContent());
        assertEquals(HttpStatus.OK, rolePermissionModelActionResponse.getHttpStatus());
    }

    @Test
    public void readWithoutChecksNotFoundTest() {
        Mockito.when(roleAccessor.getRoles(Mockito.anyCollection())).thenReturn(Set.of());

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap, List.of(descriptorKey));
        ActionResponse<RolePermissionModel> rolePermissionModelActionResponse = roleActions.readWithoutChecks(1L);

        assertTrue(rolePermissionModelActionResponse.isError());
        assertFalse(rolePermissionModelActionResponse.hasContent());
        assertEquals(HttpStatus.NOT_FOUND, rolePermissionModelActionResponse.getHttpStatus());
    }

    @Test
    public void testWithoutChecksTest() {
        PermissionModel permissionModel = createPermissionModel();
        RolePermissionModel rolePermissionModel = new RolePermissionModel(id, roleName, Set.of(permissionModel));

        Mockito.when(roleAccessor.doesRoleNameExist(Mockito.eq(roleName))).thenReturn(false);

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap, List.of(descriptorKey));
        ValidationActionResponse validationActionResponse = roleActions.testWithoutChecks(rolePermissionModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
    }

    @Test
    public void validateWithoutChecksTest() {
        PermissionModel permissionModel = createPermissionModel();
        RolePermissionModel rolePermissionModel = new RolePermissionModel(id, roleName, Set.of(permissionModel));

        Mockito.when(roleAccessor.doesRoleNameExist(Mockito.eq(roleName))).thenReturn(false);

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap, List.of(descriptorKey));
        ValidationActionResponse validationActionResponse = roleActions.validateWithoutChecks(rolePermissionModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
    }

    @Test
    public void validateWithoutChecksMissingRoleNameTest() {
        PermissionModel permissionModel = createPermissionModel();
        RolePermissionModel rolePermissionModel = new RolePermissionModel(id, "", Set.of(permissionModel));

        Mockito.when(roleAccessor.doesRoleNameExist(Mockito.eq(roleName))).thenReturn(true);

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap, List.of(descriptorKey));
        ValidationActionResponse validationActionResponse = roleActions.validateWithoutChecks(rolePermissionModel);

        assertTrue(validationActionResponse.isError());
        assertEquals(HttpStatus.BAD_REQUEST, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
    }

    @Test
    public void validateWithoutChecksBadRequestTest() {
        PermissionModel permissionModel = createPermissionModel();
        RolePermissionModel rolePermissionModel = new RolePermissionModel(id, roleName, Set.of(permissionModel));

        Mockito.when(roleAccessor.doesRoleNameExist(Mockito.eq(roleName))).thenReturn(true);

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap, List.of(descriptorKey));
        ValidationActionResponse validationActionResponse = roleActions.validateWithoutChecks(rolePermissionModel);

        assertTrue(validationActionResponse.isError());
        assertEquals(HttpStatus.BAD_REQUEST, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
    }

    @Test
    public void updateWithoutChecksTest() throws Exception {
        String newRoleName = "newRoleName";
        Long longId = 1L;
        PermissionModel permissionModel = createPermissionModel();
        RolePermissionModel rolePermissionModel = new RolePermissionModel(id, newRoleName, Set.of(permissionModel));
        UserRoleModel userRoleModel = new UserRoleModel(longId, roleName, false, PermissionModelUtil.convertToPermissionMatrixModel(Set.of(permissionModel)));

        Mockito.when(roleAccessor.getRoles(Mockito.anyCollection())).thenReturn(Set.of(userRoleModel));
        Mockito.when(roleAccessor.getRoles()).thenReturn(Set.of());

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap, List.of(descriptorKey));
        ActionResponse<RolePermissionModel> rolePermissionModelActionResponse = roleActions.updateWithoutChecks(1L, rolePermissionModel);

        Mockito.verify(roleAccessor).updateRoleName(Mockito.eq(longId), Mockito.eq(newRoleName));
        Mockito.verify(authorizationManager).updatePermissionsForRole(Mockito.anyString(), Mockito.any());

        assertTrue(rolePermissionModelActionResponse.isSuccessful());
        assertFalse(rolePermissionModelActionResponse.hasContent());
        assertEquals(HttpStatus.NO_CONTENT, rolePermissionModelActionResponse.getHttpStatus());
    }

    @Test
    public void updateWithoutChecksBadRequestTest() {
        PermissionModel permissionModel = createPermissionModel();
        RolePermissionModel rolePermissionModel = new RolePermissionModel(id, roleName, Set.of(permissionModel));
        UserRoleModel userRoleModel = new UserRoleModel(1L, roleName, false, PermissionModelUtil.convertToPermissionMatrixModel(Set.of(permissionModel)));
        UserRoleModel userRoleModelInUse = new UserRoleModel(2L, roleName, false, PermissionModelUtil.convertToPermissionMatrixModel(Set.of(permissionModel)));

        Mockito.when(roleAccessor.getRoles(Mockito.anyCollection())).thenReturn(Set.of(userRoleModel));
        Mockito.when(roleAccessor.getRoles()).thenReturn(Set.of(userRoleModelInUse));

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap, List.of(descriptorKey));
        ActionResponse<RolePermissionModel> rolePermissionModelActionResponse = roleActions.updateWithoutChecks(1L, rolePermissionModel);

        assertTrue(rolePermissionModelActionResponse.isError());
        assertFalse(rolePermissionModelActionResponse.hasContent());
        assertEquals(HttpStatus.BAD_REQUEST, rolePermissionModelActionResponse.getHttpStatus());
    }

    @Test
    public void updateWithoutChecksNoContentTest() {
        String newRoleName = "newRoleName";
        PermissionModel permissionModel = createPermissionModel();
        RolePermissionModel rolePermissionModel = new RolePermissionModel(id, newRoleName, Set.of(permissionModel));

        Mockito.when(roleAccessor.getRoles(Mockito.anyCollection())).thenReturn(Set.of());

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap, List.of(descriptorKey));
        ActionResponse<RolePermissionModel> rolePermissionModelActionResponse = roleActions.updateWithoutChecks(1L, rolePermissionModel);

        assertTrue(rolePermissionModelActionResponse.isError());
        assertFalse(rolePermissionModelActionResponse.hasContent());
        assertEquals(HttpStatus.NOT_FOUND, rolePermissionModelActionResponse.getHttpStatus());
    }

    @Test
    public void updateWithoutChecksErrorTest() throws Exception {
        String newRoleName = "newRoleName";
        Long longId = 1L;
        PermissionModel permissionModel = createPermissionModel();
        RolePermissionModel rolePermissionModel = new RolePermissionModel(id, newRoleName, Set.of(permissionModel));
        UserRoleModel userRoleModel = new UserRoleModel(longId, roleName, false, PermissionModelUtil.convertToPermissionMatrixModel(Set.of(permissionModel)));

        Mockito.when(roleAccessor.getRoles(Mockito.anyCollection())).thenReturn(Set.of(userRoleModel));
        Mockito.when(roleAccessor.getRoles()).thenReturn(Set.of());
        doThrow(new AlertDatabaseConstraintException("Exception for test")).when(authorizationManager).updatePermissionsForRole(Mockito.anyString(), Mockito.any());

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap, List.of(descriptorKey));
        ActionResponse<RolePermissionModel> rolePermissionModelActionResponse = roleActions.updateWithoutChecks(1L, rolePermissionModel);

        Mockito.verify(roleAccessor).updateRoleName(Mockito.eq(longId), Mockito.eq(newRoleName));

        assertTrue(rolePermissionModelActionResponse.isError());
        assertFalse(rolePermissionModelActionResponse.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, rolePermissionModelActionResponse.getHttpStatus());
    }

    @Test
    public void findExistingTest() {
        PermissionModel permissionModel = createPermissionModel();
        UserRoleModel userRoleModel = new UserRoleModel(1L, roleName, false, PermissionModelUtil.convertToPermissionMatrixModel(Set.of(permissionModel)));

        Mockito.when(roleAccessor.getRoles(Mockito.anyCollection())).thenReturn(Set.of(userRoleModel));

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap, List.of(descriptorKey));
        Optional<RolePermissionModel> rolePermissionModel = roleActions.findExisting(1L);

        assertTrue(rolePermissionModel.isPresent());
    }

    private PermissionModel createPermissionModel() {
        return new PermissionModel(roleName, context, true, true, true, true, true, true, true, true);
    }

    private DescriptorKey createDescriptorKey(String key) {
        DescriptorKey descriptorKey = new DescriptorKey() {
            @Override
            public String getUniversalKey() {
                return key;
            }

            @Override
            public String getDisplayName() {
                return key;
            }
        };
        return descriptorKey;
    }
}
