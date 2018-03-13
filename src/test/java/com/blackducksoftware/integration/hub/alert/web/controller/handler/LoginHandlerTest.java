/*
 * Copyright (C) 2018 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.web.controller.handler;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.LoginActions;
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException;

public class LoginHandlerTest {
    private final ObjectTransformer objectTransformer = new ObjectTransformer();
    private final HttpSessionCsrfTokenRepository csrfTokenRepository = new HttpSessionCsrfTokenRepository();

    @Test
    public void userLogoutWithValidSessionTest() {
        final LoginHandler loginHandler = new LoginHandler(objectTransformer, null, csrfTokenRepository);
        final HttpServletRequest request = new MockHttpServletRequest();
        final HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(30);

        final ResponseEntity<String> response = loginHandler.userLogout(request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void userLogoutWithInvalidSessionTest() {
        final LoginHandler loginHandler = new LoginHandler(objectTransformer, null, csrfTokenRepository);
        final HttpServletRequest request = new MockHttpServletRequest();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session = null;
        }

        final ResponseEntity<String> response = loginHandler.userLogout(request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void userLoginWithValidSessionTest() throws IntegrationException {
        final LoginActions loginActions = Mockito.mock(LoginActions.class);
        final LoginHandler loginHandler = new LoginHandler(objectTransformer, loginActions, csrfTokenRepository);

        final HttpServletRequest request = new MockHttpServletRequest();
        final HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(30);
        final HttpServletResponse httpResponse = new MockHttpServletResponse();
        Mockito.when(loginActions.authenticateUser(Mockito.any(), Mockito.any())).thenReturn(true);

        final ResponseEntity<String> response = loginHandler.userLogin(request, httpResponse, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void userLoginWithInvalidSessionTest() throws IntegrationException {
        final LoginActions loginActions = Mockito.mock(LoginActions.class);
        final LoginHandler loginHandler = new LoginHandler(objectTransformer, loginActions, csrfTokenRepository);

        final HttpServletRequest request = new MockHttpServletRequest();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session = null;
        }
        Mockito.when(loginActions.authenticateUser(Mockito.any(), Mockito.any())).thenReturn(false);
        final HttpServletResponse httpResponse = new MockHttpServletResponse();

        final ResponseEntity<String> response = loginHandler.userLogin(request, httpResponse, null);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void authenticateUserWithIntegrationRestExceptionTest() throws IntegrationException {
        final LoginActions loginActions = Mockito.mock(LoginActions.class);
        final LoginHandler loginHandler = new LoginHandler(objectTransformer, loginActions, csrfTokenRepository);

        final HttpStatus responseCode = HttpStatus.BAD_GATEWAY;
        Mockito.when(loginActions.authenticateUser(Mockito.any(), Mockito.any())).thenThrow(new IntegrationRestException(responseCode.value(), "", ""));

        final ResponseEntity<String> response = loginHandler.authenticateUser(null, null, null);
        assertEquals(responseCode, response.getStatusCode());
    }

    @Test
    public void authenticateUserWithAlertFieldExceptionTest() throws IntegrationException {
        final LoginActions loginActions = Mockito.mock(LoginActions.class);
        final LoginHandler loginHandler = new LoginHandler(objectTransformer, loginActions, csrfTokenRepository);

        Mockito.when(loginActions.authenticateUser(Mockito.any(), Mockito.any())).thenThrow(new AlertFieldException(Collections.emptyMap()));

        final ResponseEntity<String> response = loginHandler.authenticateUser(null, null, null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void authenticateUserWithExceptionTest() throws IntegrationException {
        final LoginActions loginActions = Mockito.mock(LoginActions.class);
        final LoginHandler loginHandler = new LoginHandler(objectTransformer, loginActions, csrfTokenRepository);

        Mockito.when(loginActions.authenticateUser(Mockito.any(), Mockito.any())).thenThrow(new NullPointerException());

        final ResponseEntity<String> response = loginHandler.authenticateUser(null, null, null);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

}
