package com.synopsys.integration.alert.web.api.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.accessor.AuthorizationUtility;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.persistence.accessor.AuthenticationTypeAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.AuthenticationTypeDetails;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.users.UserSystemValidator;
import com.synopsys.integration.alert.util.AlertFieldStatusConverter;

public class UserActionsTest {
    private final Long id = 1L;
    private final String name = "user";
    private final String password = "password";
    private final Set<UserRoleModel> roles = Set.of();
    private final AuthenticationType authenticationType = AuthenticationType.DATABASE;

    private UserAccessor userAccessor;
    private AuthorizationUtility authorizationUtility;
    private AuthorizationManager authorizationManager;
    private AuthenticationTypeAccessor authenticationTypeAccessor;
    private UserSystemValidator userSystemValidator;

    @BeforeEach
    public void init() {
        userAccessor = Mockito.mock(UserAccessor.class);
        authorizationUtility = Mockito.mock(AuthorizationUtility.class);
        authorizationManager = Mockito.mock(AuthorizationManager.class);
        authenticationTypeAccessor = Mockito.mock(AuthenticationTypeAccessor.class);
        userSystemValidator = Mockito.mock(UserSystemValidator.class);
    }

    @Test
    public void testGetUsers() {
        UserModel userModel = UserModel.existingUser(id, name, password, null, authenticationType, roles, true);
        Mockito.when(userAccessor.getUsers()).thenReturn(List.of(userModel));
        AuthenticationTypeDetails authenticationTypeDetails = new AuthenticationTypeDetails(1L, authenticationType.name());
        Mockito.when(authenticationTypeAccessor.getAuthenticationTypeDetails(Mockito.any())).thenReturn(Optional.of(authenticationTypeDetails));

        UserActions userActions = new UserActions(userAccessor, authorizationUtility, authorizationManager, authenticationTypeAccessor, userSystemValidator);
        List<UserConfig> userModels = userActions.getUsers();

        assertEquals(1, userModels.size());
        UserConfig userConfig = userModels.get(0);
        assertEquals(name, userConfig.getUsername());
        assertEquals(authenticationType.name(), userConfig.getAuthenticationType());
        assertNull(userConfig.getPassword());
    }

    @Test
    public void testInternalUserNoEmailValidation() throws Exception {
        UserModel userModel = UserModel.existingUser(id, name, password, null, authenticationType, roles, true);

        Mockito.when(userAccessor.getUser(Mockito.anyLong())).thenReturn(Optional.of(userModel));

        Set<String> roleNames = roles
                                    .stream()
                                    .map(UserRoleModel::getName)
                                    .collect(Collectors.toSet());
        UserConfig userConfig = new UserConfig(id.toString(), name, "newPassword", null, roleNames, false, false, false, true, false, authenticationType.name(), false);
        UserActions userActions = new UserActions(userAccessor, authorizationUtility, authorizationManager, authenticationTypeAccessor, userSystemValidator);
        try {
            UserConfig newConfig = userActions.updateUser(id, userConfig);
            fail("Email address is missing and should be validated.");
        } catch (AlertFieldException ex) {
            assertTrue(AlertFieldStatusConverter.convertToStringMap(ex.getFieldErrors()).containsKey(UserActions.FIELD_KEY_USER_MGMT_EMAILADDRESS));
        }
    }

    @Test
    public void testExternalUserNoEmailValidation() throws Exception {
        AuthenticationType authenticationTypeLDAP = AuthenticationType.LDAP;

        UserModel userModel = UserModel.existingUser(id, name, password, null, authenticationTypeLDAP, roles, true);

        Mockito.when(userAccessor.getUser(Mockito.anyLong())).thenReturn(Optional.of(userModel));

        Set<String> roleNames = roles
                                    .stream()
                                    .map(UserRoleModel::getName)
                                    .collect(Collectors.toSet());
        UserConfig userConfig = new UserConfig(id.toString(), name, "newPassword", null, roleNames, false, false, false, true, false, authenticationTypeLDAP.name(), true);
        UserActions userActions = new UserActions(userAccessor, authorizationUtility, authorizationManager, authenticationTypeAccessor, userSystemValidator);
        UserConfig newConfig = userActions.updateUser(id, userConfig);
        assertEquals(String.valueOf(id), newConfig.getId());
        assertEquals(name, newConfig.getUsername());
        assertNull(newConfig.getPassword());
        assertNull(newConfig.getEmailAddress());
    }
}
