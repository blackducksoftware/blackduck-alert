package com.synopsys.integration.alert.web.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.accessor.AuthorizationUtility;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.persistence.accessor.AuthenticationTypeAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.web.model.UserConfig;

public class UserActionTest {

    @Test
    public void testInternalUserNoEmailValidation() throws Exception {
        Long id = 1L;
        String name = "user";
        String password = "password";
        Set<UserRoleModel> roles = Set.of();
        AuthenticationType authenticationType = AuthenticationType.DATABASE;

        UserModel userModel = UserModel.existingUser(id, name, password, null, authenticationType, roles, true);

        UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        Mockito.when(userAccessor.getUser(Mockito.anyLong())).thenReturn(Optional.of(userModel));
        AuthorizationUtility authorizationUtility = Mockito.mock(AuthorizationUtility.class);
        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        AuthenticationTypeAccessor authenticationTypeAccessor = Mockito.mock(AuthenticationTypeAccessor.class);

        Set<String> roleNames = roles
                                    .stream()
                                    .map(UserRoleModel::getName)
                                    .collect(Collectors.toSet());
        UserConfig userConfig = new UserConfig(id.toString(), name, "newPassword", null, roleNames, false, false, false, true, false, authenticationType.name(), true);
        UserActions userActions = new UserActions(userAccessor, authorizationUtility, authorizationManager, authenticationTypeAccessor);
        try {
            UserConfig newConfig = userActions.updateUser(id, userConfig);
            fail("Email adress is missing and should be validated.");
        } catch (AlertFieldException ex) {
            assertTrue(ex.getFieldErrors().containsKey(UserActions.FIELD_KEY_USER_MGMT_EMAILADDRESS));
        }
    }

    @Test
    public void testExternalUserNoEmailValidation() throws Exception {
        Long id = 1L;
        String name = "user";
        String password = "password";
        Set<UserRoleModel> roles = Set.of();
        AuthenticationType authenticationType = AuthenticationType.LDAP;

        UserModel userModel = UserModel.existingUser(id, name, password, null, authenticationType, roles, true);

        UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        Mockito.when(userAccessor.getUser(Mockito.anyLong())).thenReturn(Optional.of(userModel));
        AuthorizationUtility authorizationUtility = Mockito.mock(AuthorizationUtility.class);
        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        AuthenticationTypeAccessor authenticationTypeAccessor = Mockito.mock(AuthenticationTypeAccessor.class);

        Set<String> roleNames = roles
                                    .stream()
                                    .map(UserRoleModel::getName)
                                    .collect(Collectors.toSet());
        UserConfig userConfig = new UserConfig(id.toString(), name, "newPassword", null, roleNames, false, false, false, true, false, authenticationType.name(), true);
        UserActions userActions = new UserActions(userAccessor, authorizationUtility, authorizationManager, authenticationTypeAccessor);
        UserConfig newConfig = userActions.updateUser(id, userConfig);
        assertEquals(String.valueOf(id), newConfig.getId());
        assertEquals(name, newConfig.getUsername());
        assertNull(newConfig.getPassword());
        assertNull(newConfig.getEmailAddress());
    }
}
