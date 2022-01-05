package com.synopsys.integration.alert.component.users.web.role;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertForbiddenOperationException;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.users.UserManagementDescriptorKey;
import com.synopsys.integration.alert.component.users.web.role.util.PermissionModelUtil;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public class RoleActionsTest {
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

        Mockito.when(authorizationManager.hasCreatePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(true);
        Mockito.when(authorizationManager.hasReadPermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(true);
        Mockito.when(authorizationManager.hasDeletePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(true);
        Mockito.when(authorizationManager.hasExecutePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(true);
        Mockito.when(authorizationManager.hasWritePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(true);

        Mockito.when(authorizationManager.hasCreatePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(authorizationManager.hasReadPermission(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(authorizationManager.hasDeletePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(authorizationManager.hasExecutePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(authorizationManager.hasWritePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

    }

    @Test
    public void createTest() throws Exception {
        PermissionModel permissionModel = createPermissionModel();
        RolePermissionModel rolePermissionModel = new RolePermissionModel(null, roleName, Set.of(permissionModel));
        UserRoleModel userRoleModel = new UserRoleModel(1L, roleName, false, PermissionModelUtil.convertToPermissionMatrixModel(Set.of(permissionModel)));

        Mockito.when(roleAccessor.doesRoleNameExist(Mockito.anyString())).thenReturn(false);
        Mockito.when(authorizationManager.createRoleWithPermissions(Mockito.eq(roleName), Mockito.any())).thenReturn(userRoleModel);

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap);

        ActionResponse<RolePermissionModel> rolePermissionModelActionResponse = roleActions.create(rolePermissionModel);

        assertTrue(rolePermissionModelActionResponse.isSuccessful());
        assertTrue(rolePermissionModelActionResponse.hasContent());
    }

    @Test
    public void deleteTest() throws Exception {
        PermissionModel permissionModel = createPermissionModel();
        UserRoleModel userRoleModel = new UserRoleModel(1L, roleName, false, PermissionModelUtil.convertToPermissionMatrixModel(Set.of(permissionModel)));

        Mockito.when(roleAccessor.getRoles(Mockito.anyCollection())).thenReturn(Set.of(userRoleModel));

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap);
        ActionResponse<RolePermissionModel> rolePermissionModelActionResponse = roleActions.delete(1L);

        Mockito.verify(authorizationManager).deleteRole(Mockito.anyLong());

        assertTrue(rolePermissionModelActionResponse.isSuccessful());
        assertFalse(rolePermissionModelActionResponse.hasContent());
        assertEquals(HttpStatus.NO_CONTENT, rolePermissionModelActionResponse.getHttpStatus());
    }

    @Test
    public void deleteErrorTest() throws Exception {
        PermissionModel permissionModel = createPermissionModel();
        UserRoleModel userRoleModel = new UserRoleModel(1L, roleName, false, PermissionModelUtil.convertToPermissionMatrixModel(Set.of(permissionModel)));

        Mockito.when(roleAccessor.getRoles(Mockito.anyCollection())).thenReturn(Set.of(userRoleModel));
        Mockito.doThrow(new AlertForbiddenOperationException("Exception for test")).when(authorizationManager).deleteRole(Mockito.anyLong());

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap);
        ActionResponse<RolePermissionModel> rolePermissionModelActionResponse = roleActions.delete(1L);

        assertTrue(rolePermissionModelActionResponse.isError());
        assertFalse(rolePermissionModelActionResponse.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, rolePermissionModelActionResponse.getHttpStatus());
    }

    @Test
    public void deleteNotFoundTest() {
        Mockito.when(roleAccessor.getRoles(Mockito.anyCollection())).thenReturn(Set.of());

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap);
        ActionResponse<RolePermissionModel> rolePermissionModelActionResponse = roleActions.delete(1L);

        assertTrue(rolePermissionModelActionResponse.isError());
        assertFalse(rolePermissionModelActionResponse.hasContent());
        assertEquals(HttpStatus.NOT_FOUND, rolePermissionModelActionResponse.getHttpStatus());
    }

    @Test
    public void getAllTest() {
        PermissionModel permissionModel = createPermissionModel();
        UserRoleModel userRoleModel = new UserRoleModel(1L, roleName, false, PermissionModelUtil.convertToPermissionMatrixModel(Set.of(permissionModel)));

        Mockito.when(roleAccessor.getRoles()).thenReturn(Set.of(userRoleModel));

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap);
        ActionResponse<MultiRolePermissionModel> rolePermissionModelActionResponse = roleActions.getAll();

        assertTrue(rolePermissionModelActionResponse.isSuccessful());
        assertTrue(rolePermissionModelActionResponse.hasContent());
        assertEquals(HttpStatus.OK, rolePermissionModelActionResponse.getHttpStatus());
    }

    @Test
    public void getOneTest() {
        PermissionModel permissionModel = createPermissionModel();
        UserRoleModel userRoleModel = new UserRoleModel(1L, roleName, false, PermissionModelUtil.convertToPermissionMatrixModel(Set.of(permissionModel)));

        Mockito.when(roleAccessor.getRoles(Mockito.anyCollection())).thenReturn(Set.of(userRoleModel));

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap);
        ActionResponse<RolePermissionModel> rolePermissionModelActionResponse = roleActions.getOne(1L);

        assertTrue(rolePermissionModelActionResponse.isSuccessful());
        assertTrue(rolePermissionModelActionResponse.hasContent());
        assertEquals(HttpStatus.OK, rolePermissionModelActionResponse.getHttpStatus());
    }

    @Test
    public void getOneNotFoundTest() {
        Mockito.when(roleAccessor.getRoles(Mockito.anyCollection())).thenReturn(Set.of());

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap);
        ActionResponse<RolePermissionModel> rolePermissionModelActionResponse = roleActions.getOne(1L);

        assertTrue(rolePermissionModelActionResponse.isError());
        assertFalse(rolePermissionModelActionResponse.hasContent());
        assertEquals(HttpStatus.NOT_FOUND, rolePermissionModelActionResponse.getHttpStatus());
    }

    @Test
    public void testTest() {
        PermissionModel permissionModel = createPermissionModel();
        RolePermissionModel rolePermissionModel = new RolePermissionModel(null, roleName, Set.of(permissionModel));

        Mockito.when(roleAccessor.doesRoleNameExist(Mockito.eq(roleName))).thenReturn(false);

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap);
        ValidationActionResponse validationActionResponse = roleActions.test(rolePermissionModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
    }

    @Test
    public void validateTest() {
        PermissionModel permissionModel = createPermissionModel();
        RolePermissionModel rolePermissionModel = new RolePermissionModel(null, roleName, Set.of(permissionModel));

        Mockito.when(roleAccessor.doesRoleNameExist(Mockito.eq(roleName))).thenReturn(false);

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap);
        ValidationActionResponse validationActionResponse = roleActions.validate(rolePermissionModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
    }

    @Test
    public void validateMissingRoleNameTest() {
        PermissionModel permissionModel = createPermissionModel();
        RolePermissionModel rolePermissionModel = new RolePermissionModel(null, "", Set.of(permissionModel));

        Mockito.when(roleAccessor.doesRoleNameExist(Mockito.eq(roleName))).thenReturn(true);

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap);
        ValidationActionResponse validationActionResponse = roleActions.validate(rolePermissionModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());

        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    public void validateBadRequestTest() {
        PermissionModel permissionModel = createPermissionModel();
        RolePermissionModel rolePermissionModel = new RolePermissionModel(null, roleName, Set.of(permissionModel));

        Mockito.when(roleAccessor.doesRoleNameExist(Mockito.eq(roleName))).thenReturn(true);

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap);
        ValidationActionResponse validationActionResponse = roleActions.validate(rolePermissionModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());

        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    public void validateDuplicatePermissionsTest() {
        PermissionModel permissionModel = createPermissionModel();
        PermissionModel permissionModelDuplicate = new PermissionModel(descriptorKey.getUniversalKey(), context, false, true, true, true, true, true, true, true);
        RolePermissionModel rolePermissionModel = new RolePermissionModel(null, roleName, Set.of(permissionModel, permissionModelDuplicate));

        Mockito.when(roleAccessor.doesRoleNameExist(Mockito.eq(roleName))).thenReturn(false);

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap);
        ValidationActionResponse validationActionResponse = roleActions.validate(rolePermissionModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());

        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    public void updateTest() throws Exception {
        String newRoleName = "newRoleName";
        Long longId = 1L;
        PermissionModel permissionModel = createPermissionModel();
        RolePermissionModel rolePermissionModel = new RolePermissionModel(null, newRoleName, Set.of(permissionModel));
        UserRoleModel userRoleModel = new UserRoleModel(longId, roleName, false, PermissionModelUtil.convertToPermissionMatrixModel(Set.of(permissionModel)));

        Mockito.when(roleAccessor.getRoles(Mockito.anyCollection())).thenReturn(Set.of(userRoleModel));
        Mockito.when(roleAccessor.getRoles()).thenReturn(Set.of());

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap);
        ActionResponse<RolePermissionModel> rolePermissionModelActionResponse = roleActions.update(1L, rolePermissionModel);

        Mockito.verify(authorizationManager).updateRoleName(Mockito.eq(longId), Mockito.eq(newRoleName));
        Mockito.verify(authorizationManager).updatePermissionsForRole(Mockito.anyString(), Mockito.any());

        assertTrue(rolePermissionModelActionResponse.isSuccessful());
        assertFalse(rolePermissionModelActionResponse.hasContent());
        assertEquals(HttpStatus.NO_CONTENT, rolePermissionModelActionResponse.getHttpStatus());
    }

    @Test
    public void updateNoContentTest() {
        String newRoleName = "newRoleName";
        PermissionModel permissionModel = createPermissionModel();
        RolePermissionModel rolePermissionModel = new RolePermissionModel(null, newRoleName, Set.of(permissionModel));

        Mockito.when(roleAccessor.getRoles(Mockito.anyCollection())).thenReturn(Set.of());

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap);
        ActionResponse<RolePermissionModel> rolePermissionModelActionResponse = roleActions.updateWithoutChecks(1L, rolePermissionModel);

        assertTrue(rolePermissionModelActionResponse.isError());
        assertFalse(rolePermissionModelActionResponse.hasContent());
        assertEquals(HttpStatus.NOT_FOUND, rolePermissionModelActionResponse.getHttpStatus());
    }

    @Test
    public void updateErrorTest() throws Exception {
        String newRoleName = "newRoleName";
        Long longId = 1L;
        PermissionModel permissionModel = createPermissionModel();
        RolePermissionModel rolePermissionModel = new RolePermissionModel(null, newRoleName, Set.of(permissionModel));
        UserRoleModel userRoleModel = new UserRoleModel(longId, roleName, false, PermissionModelUtil.convertToPermissionMatrixModel(Set.of(permissionModel)));

        Mockito.when(roleAccessor.getRoles(Mockito.anyCollection())).thenReturn(Set.of(userRoleModel));
        Mockito.when(roleAccessor.getRoles()).thenReturn(Set.of());
        Mockito.doThrow(new AlertConfigurationException("Exception for test")).when(authorizationManager).updatePermissionsForRole(Mockito.anyString(), Mockito.any());

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap);
        ActionResponse<RolePermissionModel> rolePermissionModelActionResponse = roleActions.update(1L, rolePermissionModel);

        Mockito.verify(authorizationManager).updateRoleName(Mockito.eq(longId), Mockito.eq(newRoleName));

        assertTrue(rolePermissionModelActionResponse.isError());
        assertFalse(rolePermissionModelActionResponse.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, rolePermissionModelActionResponse.getHttpStatus());
    }

    @Test
    public void findExistingTest() {
        PermissionModel permissionModel = createPermissionModel();
        UserRoleModel userRoleModel = new UserRoleModel(1L, roleName, false, PermissionModelUtil.convertToPermissionMatrixModel(Set.of(permissionModel)));

        Mockito.when(roleAccessor.getRoles(Mockito.anyCollection())).thenReturn(Set.of(userRoleModel));

        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, roleAccessor, authorizationManager, descriptorMap);
        Optional<RolePermissionModel> rolePermissionModel = roleActions.findExisting(1L);

        assertTrue(rolePermissionModel.isPresent());
    }

    private PermissionModel createPermissionModel() {
        return new PermissionModel(descriptorKey.getUniversalKey(), context, true, true, true, true, true, true, true, true);
    }

    private DescriptorKey createDescriptorKey(String key) {
        DescriptorKey descriptorKey = new DescriptorKey(key, key) {};
        return descriptorKey;
    }

}
