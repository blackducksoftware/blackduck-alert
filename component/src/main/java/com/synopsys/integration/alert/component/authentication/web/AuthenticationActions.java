/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.component.authentication.security.AlertAuthenticationProvider;

@Component
public class AuthenticationActions {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationActions.class);
    private final AlertAuthenticationProvider authenticationProvider;
    private final CsrfTokenRepository csrfTokenRepository;

    @Autowired
    public AuthenticationActions(AlertAuthenticationProvider authenticationProvider, CsrfTokenRepository csrfTokenRepository) {
        this.authenticationProvider = authenticationProvider;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    public ActionResponse<Void> authenticateUser(HttpServletRequest servletRequest, HttpServletResponse servletResponse, LoginConfig loginConfig) throws BadCredentialsException {
        ActionResponse<Void> response = new ActionResponse<>(HttpStatus.UNAUTHORIZED);
        try {
            Authentication pendingAuthentication = createUsernamePasswordAuthToken(loginConfig);
            Authentication authentication = authenticationProvider.authenticate(pendingAuthentication);
            boolean authenticated = authentication.isAuthenticated() && !authentication.getAuthorities().isEmpty();

            if (authenticated) {
                CsrfToken token = csrfTokenRepository.generateToken(servletRequest);
                csrfTokenRepository.saveToken(token, servletRequest, servletResponse);
                servletResponse.setHeader(token.getHeaderName(), token.getToken());
                response = new ActionResponse<>(HttpStatus.NO_CONTENT);
            } else {
                servletRequest.getSession().invalidate();
            }
        } catch (AuthenticationException ex) {
            logger.error("Error Authenticating user.", ex);
        }
        return response;
    }

    public ActionResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        response.addHeader("Location", "/");
        return new ActionResponse<>(HttpStatus.NO_CONTENT);
    }

    private UsernamePasswordAuthenticationToken createUsernamePasswordAuthToken(LoginConfig loginConfig) {
        return new UsernamePasswordAuthenticationToken(loginConfig.getAlertUsername(), loginConfig.getAlertPassword());
    }

}
