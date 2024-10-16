/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.authentication.security;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.authentication.security.AuthenticationPerformer;

@Component
public class AlertAuthenticationProvider implements AuthenticationProvider {
    private final Logger logger = LoggerFactory.getLogger(AlertAuthenticationProvider.class);
    private List<AuthenticationPerformer> authenticationPerformers;

    @Autowired
    public AlertAuthenticationProvider(List<AuthenticationPerformer> authenticationPerformers) {
        this.authenticationPerformers = authenticationPerformers;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            throw new IllegalArgumentException("Only UsernamePasswordAuthenticationToken is supported, " + authentication.getClass() + " was attempted");
        }

        for (AuthenticationPerformer authenticationPerformer : authenticationPerformers) {
            try {
                Optional<Authentication> completedAuthentication = authenticationPerformer.performAuthentication(authentication)
                                                                       .filter(Authentication::isAuthenticated);
                if (completedAuthentication.isPresent()) {
                    return completedAuthentication.get();
                }
            } catch (LockedException ex) {
                String authTypeError = String.format("Error with with authentication type %s - cause: %s", authenticationPerformer.getAuthenticationType(), ex.getMessage());
                logger.error(authTypeError);
                logger.debug(ex.getMessage(), ex);
                throw ex;
            } catch (Exception ex) {
                String authTypeError = String.format("Error with with authentication type %s - cause: %s", authenticationPerformer.getAuthenticationType(), ex.getMessage());
                logger.error(authTypeError);
                logger.debug(ex.getMessage(), ex);
            }
        }

        return authentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
