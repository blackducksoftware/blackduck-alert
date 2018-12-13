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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.TestProperties;
import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.common.LdapProperties;
import com.synopsys.integration.alert.database.api.user.UserAccessor;
import com.synopsys.integration.alert.database.api.user.UserModel;
import com.synopsys.integration.alert.database.user.UserRepository;
import com.synopsys.integration.alert.mock.model.MockLoginRestModel;
import com.synopsys.integration.alert.web.security.authentication.ldap.LdapManager;
import com.synopsys.integration.test.annotation.HubConnectionTest;

@Category(HubConnectionTest.class)
public class LoginActionsTestIT extends AlertIntegrationTest {
    private final Logger logger = LoggerFactory.getLogger(LoginActionsTestIT.class);
    private final MockLoginRestModel mockLoginRestModel = new MockLoginRestModel();
    private final TestProperties properties = new TestProperties();
    @Autowired
    private DaoAuthenticationProvider alertDatabaseAuthProvider;
    @Autowired
    private UserAccessor userAccessor;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LdapManager ldapManager;

    @Before
    public void init() {
        final LdapProperties ldapProperties = new LdapProperties();
        ldapProperties.setEnabled("false");
        ldapManager.updateConfiguration(ldapProperties);
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
        userAccessor.addUser(userName, mockLoginRestModel.getBlackDuckPassword());
        final boolean userAuthenticated = loginActions.authenticateUser(mockLoginRestModel.createRestModel());

        assertTrue(userAuthenticated);
        final Optional<UserModel> userModel = userAccessor.getUser(userName);
        assertTrue(userModel.isPresent());
        final UserModel model = userModel.get();
        assertFalse(model.hasRole("ADMIN"));
        assertTrue(model.getRoles().isEmpty());

        userAccessor.deleteUser(userName);
    }

    @Test
    public void testAuthenticationLDAPUserIT() {
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

}
