package com.synopsys.integration.alert.web.api.role;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.accessor.AuthorizationUtility;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.users.UserManagementDescriptorKey;

public class RoleActionsTest {
    private final String id = "1L";
    private final String roleName = "roleName";
    private final String context = "GLOBAL";

    private UserManagementDescriptorKey userManagementDescriptorKey;
    private AuthorizationUtility authorizationUtility;
    private AuthorizationManager authorizationManager;
    private DescriptorMap descriptorMap;

    @BeforeEach
    public void init() {
        userManagementDescriptorKey = Mockito.mock(UserManagementDescriptorKey.class);
        authorizationUtility = Mockito.mock(AuthorizationUtility.class);
        authorizationManager = Mockito.mock(AuthorizationManager.class);
        descriptorMap = Mockito.mock(DescriptorMap.class);
    }

    @Test
    public void createWithoutChecksTest() throws Exception {
        PermissionModel permissionModel = createPermissionModel();
        RolePermissionModel rolePermissionModel = new RolePermissionModel(id, roleName, Set.of(permissionModel));
        //UserRoleModel userRoleModel = new UserRoleModel(1L, roleName, false, );

        //TODO: UserRoleModel.of will return a UserRoleModel without permissions.
        // To return a userRoleModel we would need to convert Set<PermissionModel>
        // into a PermissionMatrixModel of permissions
        Mockito.when(authorizationUtility.createRoleWithPermissions(Mockito.eq(roleName), Mockito.any())).thenReturn(UserRoleModel.of(roleName));

        DescriptorKey descriptorKey = createDescriptorKey("descriptorKey-test");
        RoleActions roleActions = new RoleActions(userManagementDescriptorKey, authorizationUtility, authorizationManager, descriptorMap, List.of(descriptorKey));

        ActionResponse<RolePermissionModel> rolePermissionModelActionResponse = roleActions.createWithoutChecks(rolePermissionModel);

        Mockito.verify(authorizationManager).loadPermissionsIntoCache();
        assertTrue(rolePermissionModelActionResponse.isSuccessful());
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
