/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.authentication.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.component.authentication.security.AlertAuthenticationProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AuthenticationActions {
    public static final String ERROR_LOGIN_ATTEMPT_FAILED = "Login attempt failed.";
    public static final String ERROR_ACCOUNT_TEMPORARILY_LOCKED = "Account temporarily locked.";
    private final Logger logger = LoggerFactory.getLogger(AuthenticationActions.class);
    private final AlertAuthenticationProvider authenticationProvider;
    private final CsrfTokenRepository csrfTokenRepository;
    private final SecurityContextRepository securityContextRepository;

    @Autowired
    public AuthenticationActions(AlertAuthenticationProvider authenticationProvider, CsrfTokenRepository csrfTokenRepository, SecurityContextRepository securityContextRepository) {
        this.authenticationProvider = authenticationProvider;
        this.csrfTokenRepository = csrfTokenRepository;
        this.securityContextRepository = securityContextRepository;
    }

    public ActionResponse<AuthenticationResponseModel> authenticateUser(HttpServletRequest servletRequest, HttpServletResponse servletResponse, LoginConfig loginConfig)
        throws BadCredentialsException {
        ActionResponse<AuthenticationResponseModel> response = new ActionResponse<>(
            HttpStatus.OK,
            new AuthenticationResponseModel(HttpStatus.UNAUTHORIZED.value(), ERROR_LOGIN_ATTEMPT_FAILED)
        );
        try {
            Authentication pendingAuthentication = createUsernamePasswordAuthToken(loginConfig);
            Authentication authentication = authenticationProvider.authenticate(pendingAuthentication);
            boolean authenticated = authentication.isAuthenticated() && !authentication.getAuthorities().isEmpty();

            if (authenticated) {
                CsrfToken token = csrfTokenRepository.generateToken(servletRequest);
                csrfTokenRepository.saveToken(token, servletRequest, servletResponse);
                servletResponse.setHeader(token.getHeaderName(), token.getToken());
                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                securityContext.setAuthentication(authentication);
                securityContextRepository.saveContext(securityContext, servletRequest, servletResponse);
                response = new ActionResponse<>(HttpStatus.OK, new AuthenticationResponseModel(HttpStatus.OK.value(), ""));
            } else {
                servletRequest.getSession().invalidate();
            }
        } catch (LockedException ex) {
            response = new ActionResponse<>(HttpStatus.OK, new AuthenticationResponseModel(HttpStatus.UNAUTHORIZED.value(), ERROR_ACCOUNT_TEMPORARILY_LOCKED));
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
