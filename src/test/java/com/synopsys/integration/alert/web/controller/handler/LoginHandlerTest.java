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
package com.synopsys.integration.alert.web.controller.handler;

import static org.junit.Assert.assertEquals;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.web.actions.LoginActions;

public class LoginHandlerTest {
    private final HttpSessionCsrfTokenRepository csrfTokenRepository = new HttpSessionCsrfTokenRepository();

    @Test
    public void userLogoutWithValidSessionTest() {
        final Gson gson = new Gson();
        final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());
        final LoginHandler loginHandler = new LoginHandler(contentConverter, null, csrfTokenRepository);
        final HttpServletRequest request = new MockHttpServletRequest();
        final HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(30);

        final ResponseEntity<String> response = loginHandler.userLogout(request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void userLogoutWithInvalidSessionTest() {
        final Gson gson = new Gson();
        final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());
        final LoginHandler loginHandler = new LoginHandler(contentConverter, null, csrfTokenRepository);
        final HttpServletRequest request = new MockHttpServletRequest();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session = null;
        }

        final ResponseEntity<String> response = loginHandler.userLogout(request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void userLoginWithValidSessionTest() throws Exception {
        final LoginActions loginActions = Mockito.mock(LoginActions.class);
        final Gson gson = new Gson();
        final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());
        final LoginHandler loginHandler = new LoginHandler(contentConverter, loginActions, csrfTokenRepository);

        final HttpServletRequest request = new MockHttpServletRequest();
        final HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(30);
        final HttpServletResponse httpResponse = new MockHttpServletResponse();
        Mockito.when(loginActions.authenticateUser(Mockito.any())).thenReturn(true);

        final ResponseEntity<String> response = loginHandler.userLogin(request, httpResponse, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void userLoginWithInvalidSessionTest() throws Exception {
        final LoginActions loginActions = Mockito.mock(LoginActions.class);
        final Gson gson = new Gson();
        final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());
        final LoginHandler loginHandler = new LoginHandler(contentConverter, loginActions, csrfTokenRepository);

        final HttpServletRequest request = new MockHttpServletRequest();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session = null;
        }
        Mockito.when(loginActions.authenticateUser(Mockito.any())).thenReturn(false);
        final HttpServletResponse httpResponse = new MockHttpServletResponse();

        final ResponseEntity<String> response = loginHandler.userLogin(request, httpResponse, null);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
