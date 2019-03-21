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

import com.synopsys.integration.alert.common.exception.AlertLDAPConfigurationException;
import com.synopsys.integration.alert.common.rest.model.UserModel;
import com.synopsys.integration.alert.database.api.DefaultUserAccessor;
import com.synopsys.integration.alert.database.user.UserRole;
import com.synopsys.integration.alert.mock.model.MockLoginRestModel;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.alert.web.security.authentication.ldap.LdapManager;

@Tag(TestTags.CUSTOM_BLACKDUCK_CONNECTION)
public class LoginActionsTestIT extends AlertIntegrationTest {
    private final MockLoginRestModel mockLoginRestModel = new MockLoginRestModel();
    private final TestProperties properties = new TestProperties();
    @Autowired
    private DaoAuthenticationProvider alertDatabaseAuthProvider;
    @Autowired
    private DefaultUserAccessor userAccessor;
    @Autowired
    private LdapManager ldapManager;

    @BeforeEach
    public void init() throws Exception {
        ldapManager.updateContext();
        mockLoginRestModel.setBlackDuckUsername(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_USERNAME));
        mockLoginRestModel.setBlackDuckPassword(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_PASSWORD));
    }

    @Test
    public void testAuthenticateDBUserIT() {
        final LoginActions loginActions = new LoginActions(alertDatabaseAuthProvider, ldapManager);
        final boolean userAuthenticated = loginActions.authenticateUser(mockLoginRestModel.createRestModel());

        assertTrue(userAuthenticated);
    }

    @Test
    public void testAuthenticateDBUserFailIT() {
        mockLoginRestModel.setBlackDuckUsername(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_ACTIVE_USER));
        final LoginActions loginActions = new LoginActions(alertDatabaseAuthProvider, ldapManager);
        final MockLoginRestModel badRestModel = new MockLoginRestModel();
        badRestModel.setBlackDuckPassword("badpassword");
        try {
            loginActions.authenticateUser(badRestModel.createRestModel());
            fail();
        } catch (final BadCredentialsException ex) {

        }
    }

    @Test
    public void testAuthenticateDBUserRoleFailIT() {
        // add a user test then delete a user.
        final String userName = properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_ACTIVE_USER);
        mockLoginRestModel.setBlackDuckUsername(userName);
        final LoginActions loginActions = new LoginActions(alertDatabaseAuthProvider, ldapManager);
        userAccessor.addUser(userName, mockLoginRestModel.getBlackDuckPassword(), "");
        final boolean userAuthenticated = loginActions.authenticateUser(mockLoginRestModel.createRestModel());

        assertTrue(userAuthenticated);
        final Optional<UserModel> userModel = userAccessor.getUser(userName);
        assertTrue(userModel.isPresent());
        final UserModel model = userModel.get();
        assertFalse(model.hasRole(UserRole.ALERT_ADMIN_TEXT));
        assertTrue(model.getRoles().isEmpty());

        userAccessor.deleteUser(userName);
    }

    @Test
    public void testAuthenticationLDAPUserIT() throws Exception {
        final Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        final LdapAuthenticationProvider ldapAuthenticationProvider = Mockito.mock(LdapAuthenticationProvider.class);
        Mockito.when(ldapAuthenticationProvider.authenticate(Mockito.any(Authentication.class))).thenReturn(authentication);
        final LdapManager mockLdapManager = Mockito.mock(LdapManager.class);
        Mockito.when(mockLdapManager.isLdapEnabled()).thenReturn(true);
        Mockito.when(mockLdapManager.getAuthenticationProvider()).thenReturn(ldapAuthenticationProvider);

        final LoginActions loginActions = new LoginActions(alertDatabaseAuthProvider, mockLdapManager);
        final boolean authenticated = loginActions.authenticateUser(mockLoginRestModel.createRestModel());
        assertTrue(authenticated);
    }

    @Test
    public void testAuthenticationLDAPExceptionIT() throws Exception {
        final Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        final LdapAuthenticationProvider ldapAuthenticationProvider = Mockito.mock(LdapAuthenticationProvider.class);
        Mockito.when(ldapAuthenticationProvider.authenticate(Mockito.any(Authentication.class))).thenReturn(authentication);
        final LdapManager mockLdapManager = Mockito.mock(LdapManager.class);
        Mockito.when(mockLdapManager.isLdapEnabled()).thenReturn(true);
        Mockito.when(mockLdapManager.getAuthenticationProvider()).thenThrow(new AlertLDAPConfigurationException("LDAP CONFIG EXCEPTION"));
        final DaoAuthenticationProvider databaseProvider = Mockito.spy(alertDatabaseAuthProvider);
        final LoginActions loginActions = new LoginActions(databaseProvider, mockLdapManager);
        final boolean authenticated = loginActions.authenticateUser(mockLoginRestModel.createRestModel());
        assertTrue(authenticated);
        Mockito.verify(databaseProvider).authenticate(Mockito.any(Authentication.class));
    }

}
