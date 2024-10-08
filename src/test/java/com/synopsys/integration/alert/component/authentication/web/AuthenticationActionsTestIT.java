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
package com.synopsys.integration.alert.component.authentication.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.transaction.annotation.Transactional;

import com.blackduck.integration.alert.api.authentication.security.event.AuthenticationEventManager;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.authentication.ldap.LDAPAuthenticationPerformer;
import com.blackduck.integration.alert.authentication.ldap.action.LDAPManager;
import com.blackduck.integration.alert.component.authentication.security.AlertAuthenticationProvider;
import com.blackduck.integration.alert.component.authentication.security.database.AlertDatabaseAuthenticationPerformer;
import com.blackduck.integration.alert.component.authentication.web.AuthenticationActions;
import com.blackduck.integration.alert.component.authentication.web.AuthenticationResponseModel;
import com.blackduck.integration.alert.test.common.TestProperties;
import com.blackduck.integration.alert.test.common.TestPropertyKey;
import com.blackduck.integration.alert.test.common.TestTags;
import com.blackduck.integration.alert.common.AlertProperties;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.blackduck.integration.alert.common.enumeration.AuthenticationType;
import com.blackduck.integration.alert.common.exception.AlertForbiddenOperationException;
import com.blackduck.integration.alert.common.persistence.model.UserModel;
import com.blackduck.integration.alert.database.job.api.DefaultUserAccessor;
import com.synopsys.integration.alert.mock.model.MockLoginRestModel;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.AlertIntegrationTestConstants;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Tag(TestTags.CUSTOM_BLACKDUCK_CONNECTION)
@Transactional
@AlertIntegrationTest
class AuthenticationActionsTestIT {
    private final MockLoginRestModel mockLoginRestModel = new MockLoginRestModel();
    private final TestProperties properties = new TestProperties();
    @Autowired
    private DefaultUserAccessor userAccessor;
    @Autowired
    private LDAPManager ldapManager;
    @Autowired
    private AlertAuthenticationProvider authenticationProvider;
    @Autowired
    private CsrfTokenRepository csrfTokenRepository;

    @Autowired
    private SecurityContextRepository securityContextRepository;

    @Autowired
    private AlertProperties alertProperties;

    @BeforeEach
    public void init() throws Exception {
        ldapManager.getAuthenticationProvider();
        mockLoginRestModel.setAlertUsername(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_USERNAME));
        mockLoginRestModel.setAlertPassword(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_PASSWORD));
    }

    @Test
    void testAuthenticateDBUserIT() {
        HttpServletRequest servletRequest = new MockHttpServletRequest();
        HttpServletResponse servletResponse = new MockHttpServletResponse();
        AuthenticationActions authenticationActions = new AuthenticationActions(authenticationProvider, csrfTokenRepository, securityContextRepository);
        ActionResponse<AuthenticationResponseModel> response = authenticationActions.authenticateUser(servletRequest, servletResponse, mockLoginRestModel.createRestModel());
        AuthenticationResponseModel responseModel = response.getContent().orElseThrow(() -> new AssertionError("Authentication response expected but not found."));
        assertTrue(response.isSuccessful());
        assertTrue(response.hasContent());
        assertEquals(responseModel.getStatusCode(), HttpStatus.OK.value());
        assertTrue(StringUtils.isBlank(responseModel.getMessage()));
    }

    @Test
    void testAuthenticateDBUserFailIT() {
        HttpServletRequest servletRequest = new MockHttpServletRequest();
        HttpServletResponse servletResponse = new MockHttpServletResponse();
        mockLoginRestModel.setAlertUsername(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_ACTIVE_USER));
        AuthenticationActions authenticationActions = new AuthenticationActions(authenticationProvider, csrfTokenRepository, securityContextRepository);
        MockLoginRestModel badRestModel = new MockLoginRestModel();
        badRestModel.setAlertPassword("badpassword");
        ActionResponse<AuthenticationResponseModel> response = authenticationActions.authenticateUser(servletRequest, servletResponse, badRestModel.createRestModel());
        AuthenticationResponseModel responseModel = response.getContent().orElseThrow(() -> new AssertionError("Authentication response expected but not found."));
        assertTrue(response.isSuccessful());
        assertTrue(response.hasContent());
        assertEquals(responseModel.getStatusCode(), HttpStatus.UNAUTHORIZED.value());
        assertEquals(AuthenticationActions.ERROR_LOGIN_ATTEMPT_FAILED, responseModel.getMessage());
    }

    @Test
    void testAuthenticateDBUserRoleFailIT() throws AlertForbiddenOperationException, AlertConfigurationException {
        HttpServletRequest servletRequest = new MockHttpServletRequest();
        HttpServletResponse servletResponse = new MockHttpServletResponse();
        // add a user test then delete a user.
        String userName = String.format("testuser_%s", UUID.randomUUID());
        mockLoginRestModel.setAlertUsername(userName);
        AuthenticationActions authenticationActions = new AuthenticationActions(authenticationProvider, csrfTokenRepository, securityContextRepository);
        userAccessor.addUser(userName, mockLoginRestModel.getAlertPassword(), "");
        ActionResponse<AuthenticationResponseModel> response = authenticationActions.authenticateUser(servletRequest, servletResponse, mockLoginRestModel.createRestModel());

        AuthenticationResponseModel responseModel = response.getContent().orElseThrow(() -> new AssertionError("Authentication response expected but not found."));
        assertTrue(response.isSuccessful());
        assertTrue(response.hasContent());
        assertEquals(responseModel.getStatusCode(), HttpStatus.UNAUTHORIZED.value());
        assertEquals(AuthenticationActions.ERROR_LOGIN_ATTEMPT_FAILED, responseModel.getMessage());
        Optional<UserModel> userModel = userAccessor.getUser(userName);
        assertTrue(userModel.isPresent());
        UserModel model = userModel.get();
        assertFalse(model.hasRole(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN));
        assertTrue(model.getRoles().isEmpty());

        userAccessor.deleteUser(userName);
    }

    @Test
    void testAuthenticationLDAPUserIT() throws Exception {
        HttpServletRequest servletRequest = new MockHttpServletRequest();
        HttpServletResponse servletResponse = new MockHttpServletResponse();
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        LdapAuthenticationProvider ldapAuthenticationProvider = Mockito.mock(LdapAuthenticationProvider.class);
        Mockito.when(ldapAuthenticationProvider.authenticate(Mockito.any(Authentication.class))).thenReturn(authentication);
        LDAPManager mockLDAPManager = Mockito.mock(LDAPManager.class);
        Mockito.when(mockLDAPManager.isLDAPEnabled()).thenReturn(true);
        Mockito.when(mockLDAPManager.getAuthenticationProvider()).thenReturn(Optional.of(ldapAuthenticationProvider));

        AuthenticationActions authenticationActions = new AuthenticationActions(authenticationProvider, csrfTokenRepository, securityContextRepository);
        ActionResponse<AuthenticationResponseModel> response = authenticationActions.authenticateUser(servletRequest, servletResponse, mockLoginRestModel.createRestModel());
        AuthenticationResponseModel responseModel = response.getContent().orElseThrow(() -> new AssertionError("Authentication response expected but not found."));
        assertTrue(response.isSuccessful());
        assertTrue(response.hasContent());
        assertEquals(responseModel.getStatusCode(), HttpStatus.OK.value());
        assertTrue(StringUtils.isBlank(responseModel.getMessage()));
    }

    @Test
    void testAuthenticationLDAPExceptionIT() throws Exception {
        HttpServletRequest servletRequest = new MockHttpServletRequest();
        HttpServletResponse servletResponse = new MockHttpServletResponse();
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        LdapAuthenticationProvider ldapAuthenticationProvider = Mockito.mock(LdapAuthenticationProvider.class);
        Mockito.when(ldapAuthenticationProvider.authenticate(Mockito.any(Authentication.class))).thenReturn(authentication);
        LDAPManager mockLDAPManager = Mockito.mock(LDAPManager.class);
        Mockito.when(mockLDAPManager.isLDAPEnabled()).thenReturn(true);
        Mockito.when(mockLDAPManager.getAuthenticationProvider()).thenThrow(new AlertConfigurationException("LDAP CONFIG EXCEPTION"));
        DaoAuthenticationProvider databaseProvider = Mockito.mock(DaoAuthenticationProvider.class);
        Mockito.when(databaseProvider.authenticate(Mockito.any(Authentication.class))).thenReturn(authentication);
        AuthenticationEventManager authenticationEventManager = Mockito.mock(AuthenticationEventManager.class);
        Mockito.doNothing().when(authenticationEventManager).sendAuthenticationEvent(Mockito.any(), Mockito.eq(AuthenticationType.LDAP));
        RoleAccessor roleAccessor = Mockito.mock(RoleAccessor.class);

        AlertDatabaseAuthenticationPerformer alertDatabaseAuthenticationPerformer = new AlertDatabaseAuthenticationPerformer(
            authenticationEventManager,
            roleAccessor,
            databaseProvider,
            userAccessor,
            alertProperties
        );
        LDAPAuthenticationPerformer ldapAuthenticationPerformer = new LDAPAuthenticationPerformer(authenticationEventManager, roleAccessor, mockLDAPManager);
        AlertAuthenticationProvider testAuthenticationProvider = new AlertAuthenticationProvider(List.of(ldapAuthenticationPerformer, alertDatabaseAuthenticationPerformer));

        AuthenticationActions authenticationActions = new AuthenticationActions(testAuthenticationProvider, csrfTokenRepository, securityContextRepository);
        ActionResponse<AuthenticationResponseModel> response = authenticationActions.authenticateUser(servletRequest, servletResponse, mockLoginRestModel.createRestModel());
        AuthenticationResponseModel responseModel = response.getContent().orElseThrow(() -> new AssertionError("Authentication response expected but not found."));
        assertTrue(response.isSuccessful());
        assertTrue(response.hasContent());
        assertEquals(responseModel.getStatusCode(), HttpStatus.UNAUTHORIZED.value());
        assertEquals(AuthenticationActions.ERROR_LOGIN_ATTEMPT_FAILED, responseModel.getMessage());
        Mockito.verify(databaseProvider).authenticate(Mockito.any(Authentication.class));
    }

    @Test
    void testAuthenticationLDAPLockedExceptionIT() throws Exception {
        HttpServletRequest servletRequest = new MockHttpServletRequest();
        HttpServletResponse servletResponse = new MockHttpServletResponse();
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        LdapAuthenticationProvider ldapAuthenticationProvider = Mockito.mock(LdapAuthenticationProvider.class);
        Mockito.when(ldapAuthenticationProvider.authenticate(Mockito.any(Authentication.class))).thenReturn(authentication);
        LDAPManager mockLDAPManager = Mockito.mock(LDAPManager.class);
        Mockito.when(mockLDAPManager.isLDAPEnabled()).thenReturn(true);
        Mockito.when(mockLDAPManager.getAuthenticationProvider()).thenThrow(new LockedException("TEST LDAP ACCOUNT LOCKED EXCEPTION"));
        DaoAuthenticationProvider databaseProvider = Mockito.mock(DaoAuthenticationProvider.class);
        Mockito.when(databaseProvider.authenticate(Mockito.any(Authentication.class))).thenReturn(authentication);
        AuthenticationEventManager authenticationEventManager = Mockito.mock(AuthenticationEventManager.class);
        Mockito.doNothing().when(authenticationEventManager).sendAuthenticationEvent(Mockito.any(), Mockito.eq(AuthenticationType.LDAP));
        RoleAccessor roleAccessor = Mockito.mock(RoleAccessor.class);

        AlertDatabaseAuthenticationPerformer alertDatabaseAuthenticationPerformer = new AlertDatabaseAuthenticationPerformer(
            authenticationEventManager,
            roleAccessor,
            databaseProvider,
            userAccessor,
            alertProperties
        );
        LDAPAuthenticationPerformer ldapAuthenticationPerformer = new LDAPAuthenticationPerformer(authenticationEventManager, roleAccessor, mockLDAPManager);
        AlertAuthenticationProvider testAuthenticationProvider = new AlertAuthenticationProvider(List.of(ldapAuthenticationPerformer, alertDatabaseAuthenticationPerformer));

        AuthenticationActions authenticationActions = new AuthenticationActions(testAuthenticationProvider, csrfTokenRepository, securityContextRepository);
        ActionResponse<AuthenticationResponseModel> response = authenticationActions.authenticateUser(servletRequest, servletResponse, mockLoginRestModel.createRestModel());
        AuthenticationResponseModel responseModel = response.getContent().orElseThrow(() -> new AssertionError("Authentication response expected but not found."));
        assertTrue(response.isSuccessful());
        assertTrue(response.hasContent());
        assertEquals(responseModel.getStatusCode(), HttpStatus.UNAUTHORIZED.value());
        assertEquals(AuthenticationActions.ERROR_ACCOUNT_TEMPORARILY_LOCKED, responseModel.getMessage());
        Mockito.verify(databaseProvider, Mockito.times(0)).authenticate(Mockito.any(Authentication.class));
    }

    @Test
    void userLogoutWithValidSessionTest() {
        AuthenticationActions authenticationActions = new AuthenticationActions(authenticationProvider, csrfTokenRepository, securityContextRepository);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        MockHttpSession session = (MockHttpSession) servletRequest.getSession(true);
        session.setMaxInactiveInterval(30);
        ActionResponse<Void> response = authenticationActions.logout(servletRequest, servletResponse);
        assertTrue(response.isSuccessful());
        assertTrue(session.isInvalid(), "Expected the session to be invalid");
        assertTrue(servletResponse.containsHeader("Location"), "Expected the response to contain a Location header");
    }

    @Test
    void userLogoutWithInvalidSessionTest() {
        AuthenticationActions authenticationActions = new AuthenticationActions(authenticationProvider, csrfTokenRepository, securityContextRepository);
        HttpServletRequest servletRequest = new MockHttpServletRequest();
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ActionResponse<Void> response = authenticationActions.logout(servletRequest, servletResponse);
        assertTrue(response.isSuccessful());
    }

    @Test
    void userLoginWithBadCredentialsTest() {
        AlertAuthenticationProvider badCredentialsAuthenticationProvider = Mockito.mock(AlertAuthenticationProvider.class);
        Mockito.when(badCredentialsAuthenticationProvider.authenticate(Mockito.any())).thenThrow(new BadCredentialsException("Bad credentials test"));
        AuthenticationActions authenticationActions = new AuthenticationActions(badCredentialsAuthenticationProvider, csrfTokenRepository, securityContextRepository);

        HttpServletRequest servletRequest = new MockHttpServletRequest();
        HttpServletResponse servletResponse = new MockHttpServletResponse();
        TestProperties testProperties = new TestProperties();
        MockLoginRestModel credentialsLoginRestModel = new MockLoginRestModel();
        credentialsLoginRestModel.setAlertUsername(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_USERNAME));
        credentialsLoginRestModel.setAlertPassword(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_PASSWORD));

        ActionResponse<AuthenticationResponseModel> response = authenticationActions.authenticateUser(servletRequest, servletResponse, credentialsLoginRestModel.createRestModel());
        AuthenticationResponseModel responseModel = response.getContent().orElseThrow(() -> new AssertionError("Authentication response expected but not found."));
        assertTrue(response.isSuccessful());
        assertTrue(response.hasContent());
        assertEquals(responseModel.getStatusCode(), HttpStatus.UNAUTHORIZED.value());
        assertEquals(AuthenticationActions.ERROR_LOGIN_ATTEMPT_FAILED, responseModel.getMessage());
    }

}
