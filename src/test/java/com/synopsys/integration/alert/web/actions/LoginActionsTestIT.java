/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.synopsys.integration.alert.web.actions;

import static junit.framework.TestCase.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;

import com.synopsys.integration.alert.common.descriptor.accessor.AuthorizationUtility;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertForbiddenOperationException;
import com.synopsys.integration.alert.common.exception.AlertLDAPConfigurationException;
import com.synopsys.integration.alert.common.persistence.model.AuthenticationType;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.database.api.DefaultUserAccessor;
import com.synopsys.integration.alert.mock.model.MockLoginRestModel;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.alert.web.security.authentication.AlertAuthenticationProvider;
import com.synopsys.integration.alert.web.security.authentication.database.AlertDatabaseAuthenticationPerformer;
import com.synopsys.integration.alert.web.security.authentication.event.AuthenticationEventManager;
import com.synopsys.integration.alert.web.security.authentication.ldap.LdapManager;

@Tag(TestTags.CUSTOM_BLACKDUCK_CONNECTION)
public class LoginActionsTestIT extends AlertIntegrationTest {
    private final MockLoginRestModel mockLoginRestModel = new MockLoginRestModel();
    private final TestProperties properties = new TestProperties();
    @Autowired
    private DefaultUserAccessor userAccessor;
    @Autowired
    private LdapManager ldapManager;
    @Autowired
    private AlertAuthenticationProvider authenticationProvider;

    @BeforeEach
    public void init() throws Exception {
        ldapManager.updateContext();
        mockLoginRestModel.setBlackDuckUsername(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_USERNAME));
        mockLoginRestModel.setBlackDuckPassword(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_PASSWORD));
    }

    @Test
    public void testAuthenticateDBUserIT() {
        LoginActions loginActions = new LoginActions(authenticationProvider);
        boolean userAuthenticated = loginActions.authenticateUser(mockLoginRestModel.createRestModel());

        assertTrue(userAuthenticated);
    }

    @Test
    public void testAuthenticateDBUserFailIT() {
        mockLoginRestModel.setBlackDuckUsername(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_ACTIVE_USER));
        LoginActions loginActions = new LoginActions(authenticationProvider);
        MockLoginRestModel badRestModel = new MockLoginRestModel();
        badRestModel.setBlackDuckPassword("badpassword");
        try {
            loginActions.authenticateUser(badRestModel.createRestModel());
            fail();
        } catch (BadCredentialsException ex) {

        }
    }

    @Test
    public void testAuthenticateDBUserRoleFailIT() throws AlertDatabaseConstraintException, AlertForbiddenOperationException {
        // add a user test then delete a user.
        String userName = properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_ACTIVE_USER);
        mockLoginRestModel.setBlackDuckUsername(userName);
        LoginActions loginActions = new LoginActions(authenticationProvider);
        userAccessor.addUser(userName, mockLoginRestModel.getBlackDuckPassword(), "");
        boolean userAuthenticated = loginActions.authenticateUser(mockLoginRestModel.createRestModel());

        assertFalse(userAuthenticated);
        Optional<UserModel> userModel = userAccessor.getUser(userName);
        assertTrue(userModel.isPresent());
        UserModel model = userModel.get();
        assertFalse(model.hasRole(AlertIntegrationTest.ROLE_ALERT_ADMIN));
        assertTrue(model.getRoles().isEmpty());

        userAccessor.deleteUser(userName);
    }

    @Test
    public void testAuthenticationLDAPUserIT() throws Exception {
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        LdapAuthenticationProvider ldapAuthenticationProvider = Mockito.mock(LdapAuthenticationProvider.class);
        Mockito.when(ldapAuthenticationProvider.authenticate(Mockito.any(Authentication.class))).thenReturn(authentication);
        LdapManager mockLdapManager = Mockito.mock(LdapManager.class);
        Mockito.when(mockLdapManager.isLdapEnabled()).thenReturn(true);
        Mockito.when(mockLdapManager.getAuthenticationProvider()).thenReturn(ldapAuthenticationProvider);

        LoginActions loginActions = new LoginActions(authenticationProvider);
        boolean authenticated = loginActions.authenticateUser(mockLoginRestModel.createRestModel());
        assertTrue(authenticated);
    }

    @Test
    public void testAuthenticationLDAPExceptionIT() throws Exception {
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        LdapAuthenticationProvider ldapAuthenticationProvider = Mockito.mock(LdapAuthenticationProvider.class);
        Mockito.when(ldapAuthenticationProvider.authenticate(Mockito.any(Authentication.class))).thenReturn(authentication);
        LdapManager mockLdapManager = Mockito.mock(LdapManager.class);
        Mockito.when(mockLdapManager.isLdapEnabled()).thenReturn(true);
        Mockito.when(mockLdapManager.getAuthenticationProvider()).thenThrow(new AlertLDAPConfigurationException("LDAP CONFIG EXCEPTION"));
        DaoAuthenticationProvider databaseProvider = Mockito.mock(DaoAuthenticationProvider.class);
        Mockito.when(databaseProvider.authenticate(Mockito.any(Authentication.class))).thenReturn(authentication);
        AuthenticationEventManager authenticationEventManager = Mockito.mock(AuthenticationEventManager.class);
        Mockito.doNothing().when(authenticationEventManager).sendAuthenticationEvent(Mockito.any(), Mockito.eq(AuthenticationType.AUTH_TYPE_LDAP));
        AuthorizationUtility authorizationUtility = Mockito.mock(AuthorizationUtility.class);

        AlertDatabaseAuthenticationPerformer alertDatabaseAuthenticationPerformer = new AlertDatabaseAuthenticationPerformer(authenticationEventManager, authorizationUtility, databaseProvider);

        AlertAuthenticationProvider authenticationProvider = new AlertAuthenticationProvider(List.of(alertDatabaseAuthenticationPerformer));
        LoginActions loginActions = new LoginActions(authenticationProvider);
        boolean authenticated = loginActions.authenticateUser(mockLoginRestModel.createRestModel());
        assertFalse(authenticated);
        Mockito.verify(databaseProvider).authenticate(Mockito.any(Authentication.class));
    }

}
